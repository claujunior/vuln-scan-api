package BCC.ES.CLP.service;

import BCC.ES.CLP.dto.ScanRawResult;
import BCC.ES.CLP.dto.VulnerabilidadeResumo;
import BCC.ES.CLP.exceptions.ErroAoEncontrarIp;
import BCC.ES.CLP.model.Alvo;
import BCC.ES.CLP.model.FormatoSaida;
import BCC.ES.CLP.model.Vulnerabilidade;
import BCC.ES.CLP.repository.RepositoryAlvo;
import BCC.ES.CLP.repository.RepositoryVulnerabilidade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ServiceNuclei extends ScanTemplate {

    private final RepositoryVulnerabilidade repositoryVulnerabilidade;
    private final RepositoryAlvo repositoryAlvo;
    private final Map<FormatoSaida, SaidaStrategy> estrategias;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public ServiceNuclei(RepositoryVulnerabilidade repositoryVulnerabilidade,
                         RepositoryAlvo repositoryAlvo,
                         RelatorioLlmStrategy relatorioLlmStrategy,
                         JsonStrategy jsonStrategy,
                         SaidaBrutaStrategy saidaBrutaStrategy,
                         ServiceOrquestrador serviceOrquestrador) {
        super(serviceOrquestrador);
        this.repositoryVulnerabilidade = repositoryVulnerabilidade;
        this.repositoryAlvo = repositoryAlvo;
        this.estrategias = Map.of(
                FormatoSaida.RELATORIO_LLM, relatorioLlmStrategy,
                FormatoSaida.JSON,          jsonStrategy,
                FormatoSaida.RAW,           saidaBrutaStrategy
        );
    }

    @Override
    @Transactional
    public String processar(ScanRawResult resultado, FormatoSaida formato) {
        List<JsonNode> vulns = extrairResultados(resultado.rawOutput());

        if (vulns.isEmpty()) {
            // Atalho so vale para o relatorio de LLM, para nao gastar quota a toa;
            // JSON/RAW devem devolver a estrutura vazia normalmente.
            if (formato == FormatoSaida.RELATORIO_LLM) {
                return "Nenhuma vulnerabilidade encontrada para " + resultado.ip();
            }
            Map<String, Object> dados = Map.of("ip", resultado.ip(), "vulnerabilidades", List.of());
            return estrategias.get(formato).gerar(dados, "", resultado.rawOutput());
        }

        Alvo alvo = repositoryAlvo.findByIp(resultado.ip())
                .orElseThrow(() -> new ErroAoEncontrarIp("IP não encontrado: " + resultado.ip()));

        List<VulnerabilidadeResumo> resumo = new ArrayList<>();
        StringBuilder prompt = new StringBuilder();
        for (JsonNode vuln : vulns) {
            String templateId = texto(vuln.path("template-id"), "unknown");
            String nome       = texto(vuln.path("info").path("name"), "unknown");
            String severidade = texto(vuln.path("info").path("severity"), "unknown");
            String matchedAt  = texto(vuln.path("matched-at"), texto(vuln.path("host"), "unknown"));

            repositoryVulnerabilidade.save(
                    new Vulnerabilidade(null, null, templateId, nome, severidade, matchedAt, alvo));

            resumo.add(new VulnerabilidadeResumo(templateId, nome, severidade, matchedAt));
            prompt.append(templateId).append(" [").append(severidade).append("] em ")
                    .append(matchedAt).append("; ");
        }
        prompt.append(" faça um relatório sobre as vulnerabilidades encontradas e recomendações de correção");

        Map<String, Object> dados = Map.of("ip", resultado.ip(), "vulnerabilidades", resumo);
        return estrategias.get(formato).gerar(dados, prompt.toString(), resultado.rawOutput());
    }

    private List<JsonNode> extrairResultados(String ansibleOutput) {
        List<JsonNode> results = new ArrayList<>();

        try {
            JsonNode root = MAPPER.readTree(ansibleOutput);
            for (JsonNode play : root.path("plays")) {
                for (JsonNode task : play.path("tasks")) {
                    for (JsonNode hostResult : task.path("hosts")) {
                        parsearLinhas(texto(hostResult.path("msg"), ""), results);
                    }
                }
            }
        } catch (Exception ignored) {}

        if (results.isEmpty()) {
            parsearLinhas(ansibleOutput, results);
        }

        return results;
    }

    private void parsearLinhas(String texto, List<JsonNode> destino) {
        for (String linha : texto.split("\n")) {
            linha = linha.trim();
            if (!linha.contains("template-id")) continue;
            try {
                JsonNode node = MAPPER.readTree(linha);
                if (node.has("template-id")) destino.add(node);
            } catch (Exception ignored) {}
        }
    }

    private String texto(JsonNode node, String valorPadrao) {
        return (node.isMissingNode() || node.isNull()) ? valorPadrao : node.asText();
    }
}
