package BCC.ES.CLP.service;

import BCC.ES.CLP.dto.ScanRawResult;
import BCC.ES.CLP.exceptions.ErroAoEncontrarIp;
import BCC.ES.CLP.exceptions.PortasFechadas;
import BCC.ES.CLP.model.Alvo;
import BCC.ES.CLP.model.Scan;
import BCC.ES.CLP.repository.RepositoryAlvo;
import BCC.ES.CLP.repository.RepositoryScan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ServiceNmap extends ScanTemplate {

    private final RepositoryScan repositoryScan;
    private final RepositoryAlvo repositoryAlvo;
    private final LlmService llmService;

    public ServiceNmap(RepositoryScan repositoryScan,
                       RepositoryAlvo repositoryAlvo,
                       LlmService llmService,
                       ServiceOrquestrador serviceOrquestrador) {
        super(serviceOrquestrador);
        this.repositoryScan = repositoryScan;
        this.repositoryAlvo = repositoryAlvo;
        this.llmService = llmService;
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

        String contexto = "{\"host\":\"" + resultado.ip() + "\",\"portas\":\"" + portas + "\"}";
        return llmService.perguntar(contexto
                + " faça um relatorio sobre as portas e os servicos e diga as vulnerabilidades de seguranca");
    }
}
