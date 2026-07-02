package BCC.ES.CLP.dto;

/**
 * Representa os dados já interpretados (e persistidos) de um scan, antes de
 * decidir qual formato de saída o usuário quer ver.
 */
public interface DadosScan {

    /** Indica que o scan não encontrou nada relevante (portas, vulnerabilidades, ...). */
    default boolean vazio() {
        return false;
    }

    /** Mensagem a exibir quando {@link #vazio()} é verdadeiro, evitando chamar a LLM à toa. */
    default String mensagemVazia() {
        return "";
    }

    /** Prompt em linguagem natural usado pelo formato RELATORIO_LLM. */
    String promptLlm();

    /** Saída bruta (não interpretada) da ferramenta, usada pelo formato RAW. */
    String saidaBruta();
}
