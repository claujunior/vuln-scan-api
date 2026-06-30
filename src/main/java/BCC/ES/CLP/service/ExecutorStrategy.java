package BCC.ES.CLP.service;

import BCC.ES.CLP.model.Alvo;
import BCC.ES.CLP.model.Select;

public interface ExecutorStrategy{
    String[] montarComando(Alvo alvo, Select tool);
}
