package BCC.ES.CLP.service;

import BCC.ES.CLP.dto.DadosScan;
import BCC.ES.CLP.model.FormatoSaida;
import org.springframework.stereotype.Service;

/**
 * Devolve a saida pura da ferramenta (Nmap/Nuclei), sem nenhum parsing
 * ou passagem pela LLM.
 */
@Service
public class SaidaBrutaStrategy implements SaidaStrategy {

    @Override
    public FormatoSaida formato() {
        return FormatoSaida.RAW;
    }

    @Override
    public String gerar(DadosScan dados) {
        return dados.saidaBruta();
    }
}
