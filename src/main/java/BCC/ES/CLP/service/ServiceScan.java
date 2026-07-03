package BCC.ES.CLP.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import BCC.ES.CLP.model.FormatoSaida;
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
    private final Map<Select, ScanTemplate> templates;
    private final FormatoSaidaContexto formatoSaidaContexto;

    public ServiceScan(RepositoryScan repositoryScan,
                       RepositoryVulnerabilidade repositoryVulnerabilidade,
                       NiktoService serviceNikto,
                       NucleiService serviceNuclei,
                       FormatoSaidaContexto formatoSaidaContexto) {
        this.repositoryScan = repositoryScan;
        this.repositoryVulnerabilidade = repositoryVulnerabilidade;
        this.templates = Map.of(
                Select.NIKTO, serviceNikto,
                Select.NUCLEI, serviceNuclei
        );
        this.formatoSaidaContexto = formatoSaidaContexto;
    }

    @Transactional(readOnly = true)
    public List<Scan> allScan(User dono) {
        return repositoryScan.findByAlvo_Dono(dono);
    }

    @Transactional(readOnly = true)
    public List<Vulnerabilidade> allVulnerabilidades(User dono) {
        return repositoryVulnerabilidade.findByAlvo_Dono(dono);
    }

    public String seletor(Long id, Select select, String executor, FormatoSaida formato, User dono) {
        ScanTemplate template = templates.get(select);
        formatoSaidaContexto.definir(formato);
        try {
            return template.executar(id, select, executor, formato, dono);
        } finally {
            formatoSaidaContexto.limpar();
        }
    }
}
