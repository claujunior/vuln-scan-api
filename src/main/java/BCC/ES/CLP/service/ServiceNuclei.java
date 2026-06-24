package BCC.ES.CLP.service;

import BCC.ES.CLP.dto.ScanRawResult;
import BCC.ES.CLP.exceptions.ErroAoEncontrarIp;
import BCC.ES.CLP.model.Alvo;
import BCC.ES.CLP.model.Vulnerabilidade;
import BCC.ES.CLP.repository.RepositoryAlvo;
import BCC.ES.CLP.repository.RepositoryVulnerabilidade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
public class ServiceNuclei implements ScanStrategy {

    private final RepositoryVulnerabilidade repositoryVulnerabilidade;
    private final RepositoryAlvo repositoryAlvo;
    private final LlmService llmService;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public ServiceNuclei(RepositoryVulnerabilidade repositoryVulnerabilidade,
                         RepositoryAlvo repositoryAlvo,
                         LlmService llmService) {
        this.repositoryVulnerabilidade = repositoryVulnerabilidade;
        this.repositoryAlvo = repositoryAlvo;
        this.llmService = llmService;
    }

    @Transactional
    public String processar(ScanRawResult resultado) {
        List<JsonNode> vulns = extrairResultados(resultado.rawOutput());

        if (vulns.isEmpty()) {
            return "Nenhuma vulnerabilidade encontrada para " + resultado.ip();
        }

        Alvo alvo = repositoryAlvo.findByIp(resultado.ip())
                .orElseThrow(() -> new ErroAoEncontrarIp("IP não encontrado: " + resultado.ip()));

        StringBuilder resumo = new StringBuilder();
        for (JsonNode vuln : vulns) {
            String templateId = texto(vuln.path("template-id"), "unknown");
            String nome       = texto(vuln.path("info").path("name"), "unknown");
            String severidade = texto(vuln.path("info").path("severity"), "unknown");
            String matchedAt  = texto(vuln.path("matched-at"), texto(vuln.path("host"), "unknown"));

            repositoryVulnerabilidade.save(
                    new Vulnerabilidade(null, null, templateId, nome, severidade, matchedAt, alvo));

            resumo.append(templateId).append(" [").append(severidade).append("] em ")
                    .append(matchedAt).append("; ");
        }

        return llmService.perguntar(resumo
                + " faça um relatório sobre as vulnerabilidades encontradas e recomendações de correção");
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
