package BCC.ES.CLP.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum Select {
    NIKTO("nikto", "/app/infra/playbooks/nikto.yml", System.getProperty("user.dir") + "/infra"),
    NUCLEI("nuclei", "/app/infra/playbooks/nuclei.yml", System.getProperty("user.dir") + "/infra");
    
    private String tipo;
    private String ansible;
    private String infra_Dir;
}
