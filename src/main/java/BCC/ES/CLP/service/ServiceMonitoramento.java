package BCC.ES.CLP.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import BCC.ES.CLP.dto.MonitoramentoDto;
import BCC.ES.CLP.exceptions.AlvoNaoEncontradoException;
import BCC.ES.CLP.exceptions.MonitoramentoNaoEncontrado;
import BCC.ES.CLP.model.Alvo;
import BCC.ES.CLP.model.FormatoSaida;
import BCC.ES.CLP.model.Monitoramento;
import BCC.ES.CLP.model.NivelNotificacao;
import BCC.ES.CLP.model.Notificacao;
import BCC.ES.CLP.model.Select;
import BCC.ES.CLP.repository.RepositoryAlvo;
import BCC.ES.CLP.repository.RepositoryMonitoramento;
import BCC.ES.CLP.repository.RepositoryNotificacao;

/**
 * Aplicacao do framework: monitoramento continuo.
 *
 * Mantem, por Alvo, uma configuracao de auditoria programada e automatizada
 * (Monitoramento). Periodicamente (ver SchedulerMonitoramento) roda o scan
 * configurado reaproveitando o mesmo pipeline do scan manual (ServiceScan),
 * interpreta o resultado e, caso encontre algo relevante (portas abertas ou
 * vulnerabilidades), gera um relatorio via LLM e notifica a equipe pelo
 * canal escolhido — persistindo sempre uma Notificacao para consulta no
 * frontend.
 *
 * Pontos flexiveis deste framework, todos configuraveis por alvo:
 *  - ferramenta do scan agendado: NMAP ou NUCLEI;
 *  - forma de execucao do scan: bare metal ou docker (ExecutorStrategy);
 *  - canal de notificacao: log ou webhook (NotificadorStrategy).
 */
@Service
public class ServiceMonitoramento {

    private final RepositoryMonitoramento repositoryMonitoramento;
    private final RepositoryAlvo repositoryAlvo;
    private final RepositoryNotificacao repositoryNotificacao;
    private final ServiceScan serviceScan;
    private final LlmService llmService;
    private final Map<String, NotificadorStrategy> notificadores;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int INTERVALO_PADRAO_MINUTOS = 60;
    private static final String CANAL_PADRAO = "log";
    private static final String EXECUTOR_PADRAO = "baremetal";

    public ServiceMonitoramento(RepositoryMonitoramento repositoryMonitoramento,
                                 RepositoryAlvo repositoryAlvo,
                                 RepositoryNotificacao repositoryNotificacao,
                                 ServiceScan serviceScan,
                                 LlmService llmService,
                                 Map<String, NotificadorStrategy> notificadores) {
        this.repositoryMonitoramento = repositoryMonitoramento;
        this.repositoryAlvo = repositoryAlvo;
        this.repositoryNotificacao = repositoryNotificacao;
        this.serviceScan = serviceScan;
        this.llmService = llmService;
        this.notificadores = notificadores;
    }

    @Transactional(readOnly = true)
    public List<Monitoramento> listar() {
        return repositoryMonitoramento.findAll();
    }

    @Transactional
    public Monitoramento configurar(Long alvoId, MonitoramentoDto dto) {
        Alvo alvo = repositoryAlvo.findById(alvoId).orElseThrow(AlvoNaoEncontradoException::new);

        Monitoramento monitoramento = repositoryMonitoramento.findByAlvo(alvo).orElseGet(() -> {
            Monitoramento novo = new Monitoramento();
            novo.setAlvo(alvo);
            return novo;
        });

        monitoramento.setFerramenta(Select.valueOf(dto.getFerramenta().toUpperCase()));
        monitoramento.setExecutor(
                (dto.getExecutor() != null && !dto.getExecutor().isBlank()) ? dto.getExecutor() : EXECUTOR_PADRAO);
        monitoramento.setCanalNotificacao(
                (dto.getCanalNotificacao() != null && !dto.getCanalNotificacao().isBlank())
                        ? dto.getCanalNotificacao() : CANAL_PADRAO);
        monitoramento.setIntervaloMinutos(dto.getIntervaloMinutos() > 0 ? dto.getIntervaloMinutos() : INTERVALO_PADRAO_MINUTOS);
        monitoramento.setAtivo(dto.isAtivo());

        return repositoryMonitoramento.save(monitoramento);
    }

    @Transactional
    public void alternarAtivo(Long alvoId, boolean ativo) {
        Monitoramento monitoramento = repositoryMonitoramento.findByAlvo_Id(alvoId)
                .orElseThrow(MonitoramentoNaoEncontrado::new);
        monitoramento.setAtivo(ativo);
        repositoryMonitoramento.save(monitoramento);
    }

    @Transactional
    public void remover(Long alvoId) {
        Monitoramento monitoramento = repositoryMonitoramento.findByAlvo_Id(alvoId)
                .orElseThrow(MonitoramentoNaoEncontrado::new);
        repositoryMonitoramento.delete(monitoramento);
    }

    public void executarVerificacaoPorAlvo(Long alvoId) {
        Monitoramento monitoramento = repositoryMonitoramento.findByAlvo_Id(alvoId)
                .orElseThrow(MonitoramentoNaoEncontrado::new);
        executarVerificacao(monitoramento.getId());
    }

    /**
     * Chamado periodicamente pelo SchedulerMonitoramento. Roda apenas os
     * monitoramentos ativos cujo intervalo configurado ja decorreu desde a
     * ultima execucao (ou que nunca rodaram ainda).
     */
    public void verificarPendentes() {
        List<Monitoramento> ativos = repositoryMonitoramento.findByAtivoTrue();
        LocalDateTime agora = LocalDateTime.now();

        for (Monitoramento monitoramento : ativos) {
            boolean nuncaExecutou = monitoramento.getUltimaExecucao() == null;
            boolean venceu = !nuncaExecutou
                    && !agora.isBefore(monitoramento.getUltimaExecucao().plusMinutes(monitoramento.getIntervaloMinutos()));

            if (nuncaExecutou || venceu) {
                try {
                    executarVerificacao(monitoramento.getId());
                } catch (Exception e) {
                    System.out.println("[ServiceMonitoramento] Falha ao verificar monitoramento "
                            + monitoramento.getId() + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Executa uma auditoria: roda o scan configurado (reaproveitando
     * ServiceScan/ScanTemplate), avalia o resultado estruturado e, se um
     * problema for encontrado, gera um relatorio via LLM e notifica.
     */
    public void executarVerificacao(Long monitoramentoId) {
        Monitoramento monitoramento = repositoryMonitoramento.findById(monitoramentoId)
                .orElseThrow(MonitoramentoNaoEncontrado::new);
        Alvo alvo = monitoramento.getAlvo();

        try {
            String json = serviceScan.seletor(alvo.getId(), monitoramento.getFerramenta(),
                    monitoramento.getExecutor(), FormatoSaida.JSON);

            marcarExecutado(monitoramento);

            AvaliacaoResultado avaliacao = avaliar(monitoramento.getFerramenta(), json);
            if (!avaliacao.problemaEncontrado()) {
                return;
            }

            String mensagem = avaliacao.resumo();
            if (avaliacao.prompt() != null) {
                try {
                    mensagem = llmService.perguntar(avaliacao.prompt());
                } catch (Exception e) {
                    mensagem = "Relatório automático indisponível (falha ao consultar a LLM). Resumo bruto:\n"
                            + avaliacao.resumo();
                }
            }

            String destino = alvo.getIp() != null ? alvo.getIp() : alvo.getUrl();
            String titulo = "Auditoria automática encontrou um possível problema em " + destino
                    + " (" + monitoramento.getFerramenta() + ")";

            notificar(alvo, monitoramento, titulo, mensagem, NivelNotificacao.ALERTA);

        } catch (Exception e) {
            marcarExecutado(monitoramento);
            String destino = alvo.getIp() != null ? alvo.getIp() : alvo.getUrl();
            String titulo = "Falha na auditoria automática de " + destino;
            String mensagem = "Não foi possível concluir o scan agendado (" + monitoramento.getFerramenta()
                    + "): " + e.getMessage();
            notificar(alvo, monitoramento, titulo, mensagem, NivelNotificacao.ALERTA);
        }
    }

    private void marcarExecutado(Monitoramento monitoramento) {
        monitoramento.setUltimaExecucao(LocalDateTime.now());
        repositoryMonitoramento.save(monitoramento);
    }

    private void notificar(Alvo alvo, Monitoramento monitoramento, String titulo, String mensagem, NivelNotificacao nivel) {
        Notificacao notificacao = new Notificacao();
        notificacao.setAlvo(alvo);
        notificacao.setTitulo(titulo);
        notificacao.setMensagem(mensagem);
        notificacao.setNivel(nivel);
        notificacao.setLida(false);
        repositoryNotificacao.save(notificacao);

        NotificadorStrategy notificador = notificadores.getOrDefault(
                monitoramento.getCanalNotificacao(), notificadores.get(CANAL_PADRAO));
        if (notificador != null) {
            notificador.enviar(alvo, titulo, mensagem);
        }
    }

    private record AvaliacaoResultado(boolean problemaEncontrado, String resumo, String prompt) {}

    private AvaliacaoResultado avaliar(Select ferramenta, String json) {
        try {
            JsonNode root = MAPPER.readTree(json);

            if (ferramenta == Select.NUCLEI) {
                JsonNode vulns = root.path("vulnerabilidades");
                if (vulns.size() == 0) {
                    return new AvaliacaoResultado(false, "Nenhuma vulnerabilidade encontrada.", null);
                }

                StringBuilder resumo = new StringBuilder();
                StringBuilder prompt = new StringBuilder();
                for (JsonNode v : vulns) {
                    String templateId = texto(v.path("templateId"), "desconhecido");
                    String nome = texto(v.path("nome"), "desconhecido");
                    String severidade = texto(v.path("severidade"), "desconhecida");
                    String matchedAt = texto(v.path("matchedAt"), "desconhecido");

                    resumo.append("- [").append(severidade).append("] ").append(nome)
                            .append(" (").append(templateId).append(") em ").append(matchedAt).append("\n");
                    prompt.append(templateId).append(" [").append(severidade).append("] em ")
                            .append(matchedAt).append("; ");
                }
                prompt.append(" faça um relatório curto sobre essas vulnerabilidades e recomendações de correção");

                return new AvaliacaoResultado(true, resumo.toString(), prompt.toString());
            }

            // NMAP
            JsonNode portas = root.path("portas");
            if (portas.size() == 0) {
                return new AvaliacaoResultado(false, "Nenhuma porta aberta encontrada.", null);
            }

            StringBuilder resumo = new StringBuilder();
            for (JsonNode p : portas) {
                resumo.append("- ").append(texto(p, "?")).append("\n");
            }
            String host = texto(root.path("ip"), "");
            String prompt = "{\"host\":\"" + host + "\",\"portas\":\"" + portas + "\"}"
                    + " faça um relatório curto sobre as portas e os serviços e diga as vulnerabilidades de segurança";

            return new AvaliacaoResultado(true, resumo.toString(), prompt);
        } catch (Exception e) {
            return new AvaliacaoResultado(false, "Falha ao interpretar resultado do scan: " + e.getMessage(), null);
        }
    }

    private String texto(JsonNode node, String valorPadrao) {
        return (node.isMissingNode() || node.isNull()) ? valorPadrao : node.asText();
    }
}
