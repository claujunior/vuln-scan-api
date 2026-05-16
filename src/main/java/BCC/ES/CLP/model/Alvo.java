package BCC.ES.CLP.model;

import java.time.LocalDateTime;
import java.net.InetAddress;

import BCC.ES.CLP.exceptions.UrlInvalida;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Alvo")
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

    @Column(unique=true)
    private String ip;

    @Column(unique=true)
    private String url;

    @PrePersist
    private void prePersist() {
        if (dataHoraCadastro == null) {
            dataHoraCadastro = LocalDateTime.now().withNano(0);
        }
    }

    public Alvo(String url){
        this.url = url;
         try {
        InetAddress endereco = InetAddress.getByName(url);
        this.ip = endereco.getHostAddress();
        } catch (Exception e) {
        throw new UrlInvalida("URL inválida: " + url);
        }
    }
    public Alvo(String url, String ip){
        this.url = url;
        this.ip = ip;
    }
}
