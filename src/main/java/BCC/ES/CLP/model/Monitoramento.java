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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Configuracao de monitoramento continuo de um Alvo: auditorias programadas
 * e automatizadas que rodam periodicamente e notificam a equipe caso um
 * problema seja encontrado.
 *
 * Pontos flexiveis (ponto de extensao do framework, ver README):
 *  - ferramenta: NMAP ou NUCLEI (enum Select, mesmo usado no scan manual)
 *  - executor: "baremetal" ou "docker" (mesmo ExecutorStrategy do scan manual)
 *  - canalNotificacao: "log" ou "webhook" (ver NotificadorStrategy)
 */
@Entity
@Table(name = "Monitoramento")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Monitoramento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "Alvo_id", unique = true)
    private Alvo alvo;

    @Column(nullable = false)
    private boolean ativo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Select ferramenta;

    @Column(nullable = false)
    private String executor;

    @Column(nullable = false)
    private String canalNotificacao;

    @Column(nullable = false)
    private int intervaloMinutos;

    private LocalDateTime ultimaExecucao;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    private void prePersist() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now().withNano(0);
        }
    }
}
