package BCC.ES.CLP.service;

import java.util.List;

import BCC.ES.CLP.model.Alvo;
import BCC.ES.CLP.model.User;

public interface ServiceInterface {
      List<Alvo> allAlvos(User dono);
      void salvarAlvo(Alvo a, User dono);
      Alvo atualizarAlvo(Long id, Alvo dados, User dono);
      Alvo deletarAlvo(Long id, User dono);
}
