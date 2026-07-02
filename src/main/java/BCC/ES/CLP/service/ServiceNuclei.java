package BCC.ES.CLP.service;

import BCC.ES.CLP.dto.DadosNuclei;
import BCC.ES.CLP.dto.DadosScan;
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
    private final FormatoSaidaContexto formatoSaidaContexto;
    private final Map<FormatoSaida, SaidaStrategy> estrategias;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public ServiceNuclei(RepositoryVulnerabilidade repositoryVulnerabilidade,
                         RepositoryAlvo repositoryAlvo,
                         FormatoSaidaContexto formatoSaidaContexto,
                         RelatorioLlmStrategy relatorioLlmStrategy,
                         JsonStrategy jsonStrategy,
                         SaidaBrutaStrategy saidaBrutaStrategy,
                         ServiceOrquestrador serviceOrquestrador) {
        super(serviceOrquestrador);
        this.repositoryVulnerabilidade = repositoryVulnerabilidade;
        this.repositoryAlvo = repositoryAlvo;
        this.formatoSaidaContexto = formatoSaidaContexto;
        this.estrategias = Map.of(
                FormatoSaida.RELATORIO_LLM, relatorioLlmStrategy,
                FormatoSaida.JSON,          jsonStrategy,
                FormatoSaida.RAW,           saidaBrutaStrategy
        );
    }
    @Override
    @Transactional
    public String processar(ScanRawResult resultado) {
        List<JsonNode> vulns = extrairResultados(resultado.rawOutput());

        if (vulns.isEmpty()) {
            DadosScan dados = new DadosNuclei(resultado.ip(), List.of(), resultado.rawOutput());
            return estrategias.get(formatoSaidaContexto.obter()).gerar(dados);
        }

        Alvo alvo = repositoryAlvo.findByIp(resultado.ip())
                .orElseThrow(() -> new ErroAoEncontrarIp("IP não encontrado: " + resultado.ip()));

        List<VulnerabilidadeResumo> resumo = new ArrayList<>();
        for (JsonNode vuln : vulns) {
            String templateId = texto(vuln.path("template-id"), "unknown");
            String nome       = texto(vuln.path("info").path("name"), "unknown");
            String severidade = texto(vuln.path("info").path("severity"), "unknown");
            String matchedAt  = texto(vuln.path("matched-at"), texto(vuln.path("host"), "unknown"));

            repositoryVulnerabilidade.save(
                    new Vulnerabilidade(null, null, templateId, nome, severidade, matchedAt, alvo));

            resumo.add(new VulnerabilidadeResumo(templateId, nome, severidade, matchedAt));
        }

        DadosScan dados = new DadosNuclei(resultado.ip(), resumo, resultado.rawOutput());
        return estrategias.get(formatoSaidaContexto.obter()).gerar(dados);
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
