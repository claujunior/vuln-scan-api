package BCC.ES.CLP.repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import BCC.ES.CLP.model.Alvo;
import BCC.ES.CLP.model.User;

@Repository
public interface RepositoryAlvo extends JpaRepository<Alvo, Long>{
 List<Alvo> findByDono(User dono);
 Optional<Alvo> findByIdAndDono(Long id, User dono);
 Optional<Alvo> findByIpAndDono(String ip, User dono);
}
