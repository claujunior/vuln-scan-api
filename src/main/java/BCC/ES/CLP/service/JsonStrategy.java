package BCC.ES.CLP.service;

import BCC.ES.CLP.dto.DadosScan;
import BCC.ES.CLP.model.FormatoSaida;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

/**
 * Exporta os dados ja interpretados do scan (portas, vulnerabilidades, ...)
 * como JSON, sem passar pela LLM.
 */
@Service
public class JsonStrategy implements SaidaStrategy {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public FormatoSaida formato() {
        return FormatoSaida.JSON;
    }

    @Override
    public String gerar(DadosScan dados) {
        return MAPPER.writeValueAsString(dados);
    }
}
