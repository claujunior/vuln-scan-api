package BCC.ES.CLP.service;

import BCC.ES.CLP.dto.DadosScan;
import BCC.ES.CLP.model.FormatoSaida;

/**
 * Estrategia de apresentacao do resultado de um scan ja interpretado.
 * Cada implementacao decide como transformar {@link DadosScan} em texto de resposta
 * (relatorio de LLM, JSON estruturado, saida bruta, ...). Para adicionar um novo
 * formato: criar uma classe @Service que implemente esta interface, apontando para
 * um novo valor de {@link FormatoSaida}.
 */
public interface SaidaStrategy {

    FormatoSaida formato();

    String gerar(DadosScan dados);
}
