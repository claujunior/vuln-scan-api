package BCC.ES.CLP.service;

import BCC.ES.CLP.model.FormatoSaida;
import org.springframework.stereotype.Service;

/**
 * Comportamento antigo (unico existente antes do ponto flexivel de formato de
 * saida): manda o prompt montado por ServiceNmap/ServiceNuclei para a LLM e
 * devolve o relatorio em linguagem natural.
 */
@Service
public class RelatorioLlmStrategy implements SaidaStrategy {

    private final LlmService llmService;

    public RelatorioLlmStrategy(LlmService llmService) {
        this.llmService = llmService;
    }

    @Override
    public FormatoSaida formato() {
        return FormatoSaida.RELATORIO_LLM;
    }

    @Override
    public String gerar(Object dadosEstruturados, String prompt, String saidaBruta) {
        return llmService.perguntar(prompt);
    }
}
