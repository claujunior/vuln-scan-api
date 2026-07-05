package BCC.ES.CLP.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Dispara periodicamente a verificacao dos monitoramentos continuos
 * cadastrados. O intervalo de checagem deste agendador é fixo e configurável
 * via monitoramento.scheduler.fixedDelay (application.properties); o
 * intervalo de cada auditoria em si é por alvo, definido em
 * Monitoramento.intervaloMinutos.
 */
@Component
public class SchedulerMonitoramento {

    private final ServiceMonitoramento serviceMonitoramento;

    public SchedulerMonitoramento(ServiceMonitoramento serviceMonitoramento) {
        this.serviceMonitoramento = serviceMonitoramento;
    }

    @Scheduled(fixedDelayString = "${monitoramento.scheduler.fixedDelay:60000}")
    public void checarMonitoramentosPendentes() {
        serviceMonitoramento.verificarPendentes();
    }
}
