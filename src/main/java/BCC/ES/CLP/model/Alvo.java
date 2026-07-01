package BCC.ES.CLP.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Alvo", uniqueConstraints = {
        @UniqueConstraint(name = "uk_alvo_dono_ip",  columnNames = {"dono_id", "ip"}),
        @UniqueConstraint(name = "uk_alvo_dono_url", columnNames = {"dono_id", "url"})
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Alvo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataHoraCadastro;

    private String ip;

    private String url;

    // Dono do alvo: cada usuário só enxerga e opera sobre os seus próprios alvos.
    // @JsonIgnore evita vazar o usuário (e a senha) na resposta da API.
    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "dono_id", nullable = false, updatable = false)
    private User dono;

    @PrePersist
    private void prePersist() {
        if (dataHoraCadastro == null) {
            dataHoraCadastro = LocalDateTime.now().withNano(0);
        }
    }

    public Alvo(String url){
        this.url = url;
    }
    public Alvo(String url, String ip){
        this.url = url;
        this.ip = ip;
    }
}
