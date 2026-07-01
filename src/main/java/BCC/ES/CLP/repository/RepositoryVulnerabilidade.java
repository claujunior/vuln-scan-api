package BCC.ES.CLP.repository;

import BCC.ES.CLP.model.Alvo;
import BCC.ES.CLP.model.User;
import BCC.ES.CLP.model.Vulnerabilidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositoryVulnerabilidade extends JpaRepository<Vulnerabilidade, Long> {
    List<Vulnerabilidade> findByAlvo(Alvo alvo);

    List<Vulnerabilidade> findByAlvo_Dono(User dono);
}
