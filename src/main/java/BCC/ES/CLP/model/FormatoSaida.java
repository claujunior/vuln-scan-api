package BCC.ES.CLP.model;

/**
 * Formas de apresentar o resultado de um scan já processado.
 * Cada valor tem uma implementação de {@link BCC.ES.CLP.service.SaidaStrategy}
 * correspondente. Para adicionar uma nova saída (ex.: CSV, PDF), basta
 * acrescentar um valor aqui e criar uma nova classe @Service que implemente
 * SaidaStrategy apontando para esse valor em {@code formato()}.
 */
public enum FormatoSaida {
    RELATORIO_LLM,
    JSON,
    RAW
}
