package BCC.ES.CLP.service;

import BCC.ES.CLP.dto.DadosScan;
import BCC.ES.CLP.model.FormatoSaida;
import org.springframework.stereotype.Service;

/**
 * Comportamento antigo (unico existente antes desta mudanca): manda os dados
 * do scan para a LLM e devolve o relatorio em linguagem natural. Se o scan
 * nao encontrou nada relevante, evita gastar quota chamando a LLM a toa.
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
    public String gerar(DadosScan dados) {
        if (dados.vazio()) {
            return dados.mensagemVazia();
        }
        return llmService.perguntar(dados.promptLlm());
    }
}
