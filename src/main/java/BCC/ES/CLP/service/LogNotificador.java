package BCC.ES.CLP.service;

import org.springframework.stereotype.Component;

import BCC.ES.CLP.model.Alvo;

/**
 * Canal de notificacao padrao: apenas registra no log do servidor. Sempre
 * disponivel, nao depende de nenhuma configuracao externa. A notificacao em
 * si ja fica persistida no banco (tabela Notificacao) pelo ServiceMonitoramento
 * antes de chegar aqui; este componente cobre apenas a parte de "avisar em
 * tempo real" via console.
 */
@Component("log")
public class LogNotificador implements NotificadorStrategy {

    @Override
    public String canal() {
        return "log";
    }

    @Override
    public void enviar(Alvo alvo, String titulo, String mensagem) {
        String destino = alvo.getIp() != null ? alvo.getIp() : alvo.getUrl();
        System.out.println("=== [MONITORAMENTO] " + titulo + " (alvo: " + destino + ") ===");
        System.out.println(mensagem);
        System.out.println("=====================================================");
    }
}
