package BCC.ES.CLP.service;

import java.util.List;

import BCC.ES.CLP.model.Alvo;

public interface ServiceInterface {
      List<Alvo> allAlvos();
      void salvarAlvo(Alvo a);
      Alvo deletarAlvo(Long id);
}
