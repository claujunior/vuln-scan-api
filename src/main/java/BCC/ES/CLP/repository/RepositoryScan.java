package BCC.ES.CLP.repository;

import BCC.ES.CLP.model.Alvo;
import BCC.ES.CLP.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import BCC.ES.CLP.model.Scan;

import java.util.List;

@Repository
public interface RepositoryScan extends JpaRepository<Scan, Long>{
    List<Scan> findByAlvo(Alvo alvo);

    List<Scan> findByAlvo_Dono(User dono);
}