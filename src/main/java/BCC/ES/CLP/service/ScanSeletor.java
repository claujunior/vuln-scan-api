package BCC.ES.CLP.service;

import BCC.ES.CLP.dto.ScanRawResult;

public class ScanSeletor {

    private final ScanStrategy strategy;

    public ScanSeletor(ScanStrategy strategy) {
        this.strategy = strategy;
    }

    public String executar(ScanRawResult resultado) {
        return strategy.processar(resultado);
    }
}
