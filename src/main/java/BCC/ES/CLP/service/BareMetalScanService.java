package BCC.ES.CLP.service;

import org.springframework.stereotype.Component;

import BCC.ES.CLP.model.Alvo;
import BCC.ES.CLP.model.Select;

@Component("baremetal")
public class BareMetalScanService implements ExecutorStrategy {
    @Override
    public String[] montarComando(Alvo alvo, Select tool) {
        String playbook = tool.getInfra_Dir() + tool.getAnsible().replace("/app/infra", "");
        return new String[]{
                "ansible-playbook",
                "-i", alvo.getIp() + ",",
                playbook
        };
    }
}
    

