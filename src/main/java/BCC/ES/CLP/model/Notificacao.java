package BCC.ES.CLP.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Alerta gerado pelo monitoramento continuo (ver Monitoramento/ServiceMonitoramento)
 * quando uma auditoria automatica encontra um problema em um Alvo. Fica
 * persistido para a equipe visualizar no frontend, alem de ser encaminhado
 * pelo canal configurado (ver NotificadorStrategy).
 */
@Entity
@Table(name = "Notificacao")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataHora;

    @ManyToOne(optional = false)
    @JoinColumn(name = "Alvo_id")
    private Alvo alvo;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelNotificacao nivel;

    @Column(nullable = false)
    private boolean lida;

    @PrePersist
    private void prePersist() {
        if (dataHora == null) {
            dataHora = LocalDateTime.now().withNano(0);
        }
    }
}
