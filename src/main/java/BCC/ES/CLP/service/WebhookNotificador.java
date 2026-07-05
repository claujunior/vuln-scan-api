package BCC.ES.CLP.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import BCC.ES.CLP.model.Alvo;

/**
 * Canal de notificacao alternativo: envia um POST em formato de payload
 * compativel com Slack/Discord ({"text": "..."}) para uma URL de webhook
 * configuravel (monitoramento.webhook.url no application.properties/.env).
 * Se a URL nao estiver configurada, apenas registra um aviso no log — a
 * notificacao ainda fica salva no banco pelo ServiceMonitoramento.
 */
@Component("webhook")
public class WebhookNotificador implements NotificadorStrategy {

    @Value("${monitoramento.webhook.url:}")
    private String webhookUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String canal() {
        return "webhook";
    }

    @Override
    public void enviar(Alvo alvo, String titulo, String mensagem) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            System.out.println("[WebhookNotificador] monitoramento.webhook.url não configurado; notificação não enviada por webhook.");
            return;
        }
        String destino = alvo.getIp() != null ? alvo.getIp() : alvo.getUrl();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> payload = Map.of(
                "text", "*" + titulo + "*\nAlvo: " + destino + "\n" + mensagem
        );
        try {
            restTemplate.postForEntity(webhookUrl, new HttpEntity<>(payload, headers), String.class);
        } catch (Exception e) {
            System.out.println("[WebhookNotificador] Falha ao enviar webhook: " + e.getMessage());
        }
    }
}
