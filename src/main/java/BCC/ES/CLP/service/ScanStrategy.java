package BCC.ES.CLP.service;

import BCC.ES.CLP.dto.ScanRawResult;

public interface ScanStrategy {
    String processar(ScanRawResult resultado);
}