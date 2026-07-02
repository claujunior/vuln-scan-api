package BCC.ES.CLP.service;

import BCC.ES.CLP.dto.DadosNmap;
import BCC.ES.CLP.dto.DadosScan;
import BCC.ES.CLP.dto.ScanRawResult;
import BCC.ES.CLP.exceptions.ErroAoEncontrarIp;
import BCC.ES.CLP.exceptions.PortasFechadas;
import BCC.ES.CLP.model.Alvo;
import BCC.ES.CLP.model.FormatoSaida;
import BCC.ES.CLP.model.Scan;
import BCC.ES.CLP.repository.RepositoryAlvo;
import BCC.ES.CLP.repository.RepositoryScan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ServiceNmap extends ScanTemplate {

    private final RepositoryScan repositoryScan;
    private final RepositoryAlvo repositoryAlvo;
    private final FormatoSaidaContexto formatoSaidaContexto;
    private final Map<FormatoSaida, SaidaStrategy> estrategias;

    public ServiceNmap(RepositoryScan repositoryScan,
                       RepositoryAlvo repositoryAlvo,
                       FormatoSaidaContexto formatoSaidaContexto,
                       RelatorioLlmStrategy relatorioLlmStrategy,
                       JsonStrategy jsonStrategy,
                       SaidaBrutaStrategy saidaBrutaStrategy,
                       ServiceOrquestrador serviceOrquestrador) {
        super(serviceOrquestrador);
        this.repositoryScan = repositoryScan;
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
        Pattern pattern = Pattern.compile("(\\d+)/tcp\\s+open\\s+(\\w+)");
        Matcher matcher = pattern.matcher(resultado.rawOutput());

        Set<String> portas = new LinkedHashSet<>();
        while (matcher.find()) {
            portas.add(matcher.group(1) + ":" + matcher.group(2));
        }

        if (portas.isEmpty()) {
            throw new PortasFechadas("Nenhuma porta aberta encontrada");
        }

        Alvo alvo = repositoryAlvo.findByIp(resultado.ip())
                .orElseThrow(() -> new ErroAoEncontrarIp("IP não encontrado: " + resultado.ip()));

        for (String porta : portas) {
            String[] parts = porta.split(":");
            repositoryScan.save(new Scan(null, null, Integer.parseInt(parts[0]), parts[1], alvo));
        }

        DadosScan dados = new DadosNmap(resultado.ip(), portas, resultado.rawOutput());
        return estrategias.get(formatoSaidaContexto.obter()).gerar(dados);
    }
}
