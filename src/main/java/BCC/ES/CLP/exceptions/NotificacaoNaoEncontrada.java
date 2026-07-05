package BCC.ES.CLP.exceptions;

public class NotificacaoNaoEncontrada extends RuntimeException {

    public NotificacaoNaoEncontrada() {
        super("Notificação não encontrada");
    }

    public NotificacaoNaoEncontrada(String message) {
        super(message);
    }
}
