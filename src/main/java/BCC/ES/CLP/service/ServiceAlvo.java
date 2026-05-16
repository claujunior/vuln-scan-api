package BCC.ES.CLP.service;

import java.net.InetAddress;
import java.util.List;

import BCC.ES.CLP.exceptions.*;
import BCC.ES.CLP.model.Scan;
import BCC.ES.CLP.model.Vulnerabilidade; 
import BCC.ES.CLP.repository.RepositoryScan;
import BCC.ES.CLP.repository.RepositoryVulnerabilidade; 
import org.springframework.stereotype.Service;
import BCC.ES.CLP.model.Alvo;
import BCC.ES.CLP.repository.RepositoryAlvo;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServiceAlvo implements ServiceInterface {
    
    private final RepositoryAlvo repositoryAlvo;
    private final RepositoryScan repositoryScan;
    private final RepositoryVulnerabilidade repositoryVulnerabilidade; 
    
    public ServiceAlvo(RepositoryAlvo repositoryAlvo,
                       RepositoryScan repositoryScan,
                       RepositoryVulnerabilidade repositoryVulnerabilidade) { 
        this.repositoryAlvo = repositoryAlvo;
        this.repositoryScan = repositoryScan;
        this.repositoryVulnerabilidade = repositoryVulnerabilidade; 
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Alvo> allAlvos() {
        return repositoryAlvo.findAll();
    }
    
    @Override
    @Transactional
    public void salvarAlvo(Alvo alvo) {
        if (alvo.getIp() == null || alvo.getIp().isEmpty()) { 
            alvo = new Alvo(alvo.getUrl());
        }
        if (repositoryAlvo.findByIp(alvo.getIp()).isPresent()) {
            throw new AlvoJaRegistradoException();
        }
        repositoryAlvo.save(alvo);
    }
    
    
    @Override
    @Transactional
    public Alvo deletarAlvo(Long id) {
        Alvo alvoEncontrado = repositoryAlvo.findById(id)
                .orElseThrow(() -> new AlvoNaoEncontradoException("Alvo não encontrado"));
        List<Vulnerabilidade> vulnerabilidades = repositoryVulnerabilidade.findByAlvo(alvoEncontrado); 
        if (!vulnerabilidades.isEmpty()) {
            repositoryVulnerabilidade.deleteAll(vulnerabilidades);
        }
        List<Scan> scans = repositoryScan.findByAlvo(alvoEncontrado); 
        if (!scans.isEmpty()) {
            repositoryScan.deleteAll(scans);
        }
        repositoryAlvo.deleteById(id);
        return alvoEncontrado;
    }
    private void validarAlvo(Alvo alvo) {
        try {
            InetAddress.getByName(alvo.getIp());
        } catch (Exception e) {
            throw new AlvoInvalido("IP inválido: " + alvo.getIp());
        }
    }
}