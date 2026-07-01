package BCC.ES.CLP.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import BCC.ES.CLP.dto.ScanRawResult;
import BCC.ES.CLP.model.Scan;
import BCC.ES.CLP.model.Select;
import BCC.ES.CLP.model.User;
import BCC.ES.CLP.model.Vulnerabilidade;
import BCC.ES.CLP.repository.RepositoryScan;
import BCC.ES.CLP.repository.RepositoryVulnerabilidade;

@Service
public class ServiceScan {

    private final RepositoryScan repositoryScan;
    private final RepositoryVulnerabilidade repositoryVulnerabilidade;
    private final ServiceOrquestrador serviceOrquestrador;
    private final NiktoService serviceNikto;
    private final Map<Select, ScanStrategy> strategies;

    public ServiceScan(RepositoryScan repositoryScan,
                       RepositoryVulnerabilidade repositoryVulnerabilidade,
                       ServiceOrquestrador serviceOrquestrador,
                       NiktoService serviceNikto,
                       NucleiService serviceNuclei) {
        this.repositoryScan = repositoryScan;
        this.repositoryVulnerabilidade = repositoryVulnerabilidade;
        this.serviceOrquestrador = serviceOrquestrador;
        this.serviceNikto = serviceNikto;
        this.strategies = Map.of(
                Select.NIKTO, serviceNikto,
                Select.NUCLEI, serviceNuclei
        );
    }

    @Transactional(readOnly = true)
    public List<Scan> allScan(User dono) {
        return repositoryScan.findByAlvo_Dono(dono);
    }

    @Transactional(readOnly = true)
    public List<Vulnerabilidade> allVulnerabilidades(User dono) {
        return repositoryVulnerabilidade.findByAlvo_Dono(dono);
    }

    public String seletor(Long id, Select select, String executor, User dono) {
        ScanRawResult resultado = serviceOrquestrador.executarScan(id, select, executor, dono).join();

        ScanStrategy strategy = strategies.get(select);
        ScanSeletor seletor = new ScanSeletor(strategy);
        return seletor.executar(resultado);
    }
}
