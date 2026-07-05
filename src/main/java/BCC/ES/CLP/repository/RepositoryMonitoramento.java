package BCC.ES.CLP.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import BCC.ES.CLP.model.Alvo;
import BCC.ES.CLP.model.Monitoramento;

@Repository
public interface RepositoryMonitoramento extends JpaRepository<Monitoramento, Long> {
    Optional<Monitoramento> findByAlvo(Alvo alvo);
    Optional<Monitoramento> findByAlvo_Id(Long alvoId);
    List<Monitoramento> findByAtivoTrue();
}
