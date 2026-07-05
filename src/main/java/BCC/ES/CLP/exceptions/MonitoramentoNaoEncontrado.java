package BCC.ES.CLP.exceptions;

public class MonitoramentoNaoEncontrado extends RuntimeException {

    public MonitoramentoNaoEncontrado() {
        super("Monitoramento não configurado para este alvo");
    }

    public MonitoramentoNaoEncontrado(String message) {
        super(message);
    }
}
