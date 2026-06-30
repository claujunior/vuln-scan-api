package BCC.ES.CLP.service;

import BCC.ES.CLP.dto.ScanRawResult;
import BCC.ES.CLP.model.Scan;
import BCC.ES.CLP.model.Select;
import BCC.ES.CLP.model.Vulnerabilidade;
import BCC.ES.CLP.repository.RepositoryScan;
import BCC.ES.CLP.repository.RepositoryVulnerabilidade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ServiceScan {

    private final RepositoryScan repositoryScan;
    private final RepositoryVulnerabilidade repositoryVulnerabilidade;
    private final ServiceOrquestrador serviceOrquestrador;
    private final ServiceNmap serviceNmap;
    private final ServiceNuclei serviceNuclei;
    private final Map<Select, ScanStrategy> strategies;

    public ServiceScan(RepositoryScan repositoryScan,
                       RepositoryVulnerabilidade repositoryVulnerabilidade,
                       ServiceOrquestrador serviceOrquestrador,
                       ServiceNmap serviceNmap,
                       ServiceNuclei serviceNuclei) {
        this.repositoryScan = repositoryScan;
        this.repositoryVulnerabilidade = repositoryVulnerabilidade;
        this.serviceOrquestrador = serviceOrquestrador;
        this.serviceNmap = serviceNmap;
        this.serviceNuclei = serviceNuclei;
        this.strategies = Map.of(
                Select.NMAP,   serviceNmap,
                Select.NUCLEI, serviceNuclei
        );
    }

    @Transactional(readOnly = true)
    public List<Scan> allScan() {
        return repositoryScan.findAll();
    }

    @Transactional(readOnly = true)
    public List<Vulnerabilidade> allVulnerabilidades() {
        return repositoryVulnerabilidade.findAll();
    }

    public String seletor(Long id, Select select, String executor) {
        ScanRawResult resultado = serviceOrquestrador.executarScan(id, select, executor).join();

        ScanStrategy strategy = strategies.get(select);
        ScanSeletor seletor = new ScanSeletor(strategy);
        return seletor.executar(resultado);
    }
}
