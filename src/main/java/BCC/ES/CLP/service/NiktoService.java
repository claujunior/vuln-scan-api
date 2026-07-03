package BCC.ES.CLP.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import BCC.ES.CLP.dto.DadosNikto;
import BCC.ES.CLP.dto.DadosScan;
import BCC.ES.CLP.dto.ScanRawResult;
import BCC.ES.CLP.dto.VulnerabilidadeResumo;
import BCC.ES.CLP.exceptions.ErroAoEncontrarIp;
import BCC.ES.CLP.model.Alvo;
import BCC.ES.CLP.model.FormatoSaida;
import BCC.ES.CLP.model.Vulnerabilidade;
import BCC.ES.CLP.repository.RepositoryAlvo;
import BCC.ES.CLP.repository.RepositoryVulnerabilidade;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class NiktoService extends ScanTemplate {

    private final RepositoryVulnerabilidade repositoryVulnerabilidade;
    private final RepositoryAlvo repositoryAlvo;
    private final FormatoSaidaContexto formatoSaidaContexto;
    private final Map<FormatoSaida, SaidaStrategy> estrategias;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // Cada achado do Nikto vem como <item id="..."> com description/uri/namelink em CDATA.
    private static final Pattern ITEM      = Pattern.compile("<item\\b([^>]*)>(.*?)</item>", Pattern.DOTALL);
    private static final Pattern ID_ATTR   = Pattern.compile("id=\"([^\"]*)\"");
    private static final Pattern DESCRICAO = Pattern.compile("<description>(.*?)</description>", Pattern.DOTALL);
    private static final Pattern URI       = Pattern.compile("<uri>(.*?)</uri>", Pattern.DOTALL);
    private static final Pattern NAMELINK  = Pattern.compile("<namelink>(.*?)</namelink>", Pattern.DOTALL);

    public NiktoService(RepositoryVulnerabilidade repositoryVulnerabilidade,
                        RepositoryAlvo repositoryAlvo,
                        FormatoSaidaContexto formatoSaidaContexto,
                        RelatorioLlmStrategy relatorioLlmStrategy,
                        ServiceOrquestrador serviceOrquestrador) {
        super(serviceOrquestrador);
        this.repositoryVulnerabilidade = repositoryVulnerabilidade;
        this.repositoryAlvo = repositoryAlvo;
        this.formatoSaidaContexto = formatoSaidaContexto;
        this.estrategias = Map.of(
                FormatoSaida.RELATORIO_LLM, relatorioLlmStrategy
        );
    }

    @Override
    @Transactional
    public String processar(ScanRawResult resultado) {
        List<Achado> achados = extrairAchados(resultado.rawOutput());

        if (achados.isEmpty()) {
            DadosScan dados = new DadosNikto(resultado.ip(), List.of(), resultado.rawOutput());
            return estrategias.get(formatoSaidaContexto.obter()).gerar(dados);
        }

        Alvo alvo = repositoryAlvo.findById(resultado.alvoId())
                .orElseThrow(() -> new ErroAoEncontrarIp("Alvo não encontrado: " + resultado.alvoId()));

        List<VulnerabilidadeResumo> resumo = new ArrayList<>();
        for (Achado a : achados) {
            String severidade = "info"; // nikto não informa severidade
            repositoryVulnerabilidade.save(
                    new Vulnerabilidade(null, null, a.templateId(), a.nome(), severidade, a.matchedAt(), alvo));

            resumo.add(new VulnerabilidadeResumo(a.templateId(), a.nome(), severidade, a.matchedAt()));
        }

        DadosScan dados = new DadosNikto(resultado.ip(), resumo, resultado.rawOutput());
        return estrategias.get(formatoSaidaContexto.obter()).gerar(dados);
    }

    private record Achado(String templateId, String nome, String matchedAt) {}

    /**
     * A saída do processo é o stdout do ansible-playbook (JSON, com o callback json),
     * e dentro dela — no campo msg da task de debug — está o XML do Nikto como string.
     * Aqui a gente desembrulha o JSON pra pegar esse XML e então extrai os <item>.
     */
    private List<Achado> extrairAchados(String ansibleOutput) {
        List<Achado> achados = new ArrayList<>();

        // 1) saída do Ansible em JSON: plays -> tasks -> hosts -> msg (contém o XML)
        try {
            JsonNode root = MAPPER.readTree(ansibleOutput);
            for (JsonNode play : root.path("plays")) {
                for (JsonNode task : play.path("tasks")) {
                    for (JsonNode host : task.path("hosts")) {
                        coletar(texto(host.path("msg")), achados);
                    }
                }
            }
        } catch (Exception ignored) {}

        // 2) fallback: o próprio texto já contém o XML do nikto
        if (achados.isEmpty()) {
            coletar(ansibleOutput, achados);
        }

        return achados;
    }

    /** Extrai cada <item> do XML do Nikto. */
    private void coletar(String xml, List<Achado> destino) {
        if (xml == null || xml.isBlank()) return;
        Matcher item = ITEM.matcher(xml);
        while (item.find()) {
            String atributos = item.group(1);
            String corpo      = item.group(2);

            String id        = primeiro(ID_ATTR, atributos, "unknown");
            String descricao = cdata(primeiro(DESCRICAO, corpo, ""));
            String namelink  = cdata(primeiro(NAMELINK, corpo, ""));
            String uri       = cdata(primeiro(URI, corpo, ""));
            String onde      = !namelink.isBlank() ? namelink : uri;

            destino.add(new Achado(
                    id,
                    descricao.isBlank() ? "unknown" : descricao,
                    onde.isBlank() ? "unknown" : onde));
        }
    }

    private String primeiro(Pattern padrao, String origem, String valorPadrao) {
        Matcher m = padrao.matcher(origem);
        return m.find() ? m.group(1).trim() : valorPadrao;
    }

    /** Remove o wrapper <![CDATA[ ... ]]> quando presente. */
    private String cdata(String s) {
        s = s.trim();
        if (s.startsWith("<![CDATA[") && s.endsWith("]]>")) {
            s = s.substring("<![CDATA[".length(), s.length() - "]]>".length());
        }
        return s.trim();
    }

    private String texto(JsonNode node) {
        return (node == null || node.isMissingNode() || node.isNull()) ? "" : node.asText();
    }
}
