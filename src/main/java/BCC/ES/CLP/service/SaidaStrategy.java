package BCC.ES.CLP.service;

import BCC.ES.CLP.model.FormatoSaida;

/**
 * Estrategia de apresentacao do resultado de um scan ja interpretado.
 * Cada implementacao decide o que fazer com os dados que ServiceNmap/ServiceNuclei
 * ja extrairam do scan (prompt para a LLM, dados estruturados, saida bruta). Para
 * adicionar um novo formato: criar uma classe @Service que implemente esta
 * interface, apontando para um novo valor de {@link FormatoSaida}.
 */
public interface SaidaStrategy {

    FormatoSaida formato();

    String gerar(Object dadosEstruturados, String prompt, String saidaBruta);
}
