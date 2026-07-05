package BCC.ES.CLP.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Corpo de requisicao para configurar o monitoramento continuo de um Alvo.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MonitoramentoDto {
    private String ferramenta;       // "NMAP" ou "NUCLEI"
    private String executor;         // "baremetal" ou "docker"
    private String canalNotificacao; // "log" ou "webhook"
    private int intervaloMinutos;
    private boolean ativo;
}
