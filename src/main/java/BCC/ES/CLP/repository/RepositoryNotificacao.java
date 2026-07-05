package BCC.ES.CLP.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import BCC.ES.CLP.model.Notificacao;

@Repository
public interface RepositoryNotificacao extends JpaRepository<Notificacao, Long> {
    List<Notificacao> findAllByOrderByDataHoraDesc();
}
