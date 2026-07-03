package BCC.ES.CLP.service;

import java.net.InetAddress;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import BCC.ES.CLP.exceptions.AlvoInvalido;
import BCC.ES.CLP.exceptions.AlvoJaRegistradoException;
import BCC.ES.CLP.exceptions.AlvoNaoEncontradoException;
import BCC.ES.CLP.exceptions.UrlInvalida;
import BCC.ES.CLP.model.Alvo;
import BCC.ES.CLP.model.Scan;
import BCC.ES.CLP.model.User;
import BCC.ES.CLP.model.Vulnerabilidade;
import BCC.ES.CLP.repository.RepositoryAlvo;
import BCC.ES.CLP.repository.RepositoryScan;
import BCC.ES.CLP.repository.RepositoryVulnerabilidade;

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
    

    @Transactional(readOnly = true)
    public List<Alvo> allAlvos(User dono) {
        return repositoryAlvo.findByDono(dono);
    }
    

    @Transactional
    public void salvarAlvo(Alvo alvo, User dono) {
        resolverEValidar(alvo);
        // Duplicidade é verificada apenas dentro dos alvos do próprio usuário.
        if (repositoryAlvo.findByIpAndDono(alvo.getIp(), dono).isPresent()) {
            throw new AlvoJaRegistradoException();
        }
        alvo.setDono(dono);
        repositoryAlvo.save(alvo);
    }

    @Transactional
    public Alvo atualizarAlvo(Long id, Alvo dados, User dono) {
        Alvo alvo = repositoryAlvo.findByIdAndDono(id, dono)
                .orElseThrow(() -> new AlvoNaoEncontradoException("Alvo não encontrado"));

        alvo.setIp(dados.getIp());
        alvo.setUrl(dados.getUrl());
        resolverEValidar(alvo);

        // Se o novo IP já existe em OUTRO alvo do mesmo dono, barra.
        repositoryAlvo.findByIpAndDono(alvo.getIp(), dono)
                .filter(existente -> !existente.getId().equals(id))
                .ifPresent(existente -> { throw new AlvoJaRegistradoException(); });

        return repositoryAlvo.save(alvo);
    }

    @Transactional
    public Alvo deletarAlvo(Long id, User dono) {
        Alvo alvoEncontrado = repositoryAlvo.findByIdAndDono(id, dono)
                .orElseThrow(() -> new AlvoNaoEncontradoException("Alvo não encontrado"));
        List<Vulnerabilidade> vulnerabilidades = repositoryVulnerabilidade.findByAlvo(alvoEncontrado);
        if (!vulnerabilidades.isEmpty()) {
            repositoryVulnerabilidade.deleteAll(vulnerabilidades);
        }
        List<Scan> scans = repositoryScan.findByAlvo(alvoEncontrado);
        if (!scans.isEmpty()) {
            repositoryScan.deleteAll(scans);
        }
        repositoryAlvo.delete(alvoEncontrado);
        return alvoEncontrado;
    }

    /** Resolve o IP a partir da URL (quando ausente) e valida IP/URL. */
    private void resolverEValidar(Alvo alvo) {
        if (alvo.getIp() == null || alvo.getIp().isEmpty()) {
            try {
                InetAddress endereco = InetAddress.getByName(alvo.getUrl());
                alvo.setIp(endereco.getHostAddress());
            } catch (Exception e) {
                throw new UrlInvalida("URL inválida: " + alvo.getUrl());
            }
        }
        try {
            InetAddress.getByName(alvo.getIp());
        } catch (Exception e) {
            throw new AlvoInvalido("IP inválido: " + alvo.getIp());
        }
    }
}
