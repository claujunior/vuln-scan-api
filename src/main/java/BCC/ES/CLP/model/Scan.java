package BCC.ES.CLP.model;

import java.time.LocalDateTime;
import java.util.Optional;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Table(name = "Scan")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Scan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataHora;

    @Column(nullable=false)
    private int porta;

    @Column(nullable=false)
    private String servico;

    @ManyToOne(optional=false)
    @JoinColumn(name = "Alvo_id") 
    private Alvo alvo;

    @PrePersist
    private void prePersist() {
        if (dataHora == null) {
            dataHora = LocalDateTime.now().withNano(0);
        }
    }
}
