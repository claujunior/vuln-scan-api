package BCC.ES.CLP.service;

import org.springframework.stereotype.Component;

import BCC.ES.CLP.model.Alvo;
import BCC.ES.CLP.model.Select;

@Component("baremetal")
public class BareMetalScanService implements ExecutorStrategy {
    @Override
    public String[] montarComando(Alvo alvo, Select tool) {
        String playbook = tool.getInfra_Dir() + tool.getAnsible().replace("/app/infra", "");
        // No host a variável ANSIBLE_STDOUT_CALLBACK=json não está setada (só existe no
        // container Docker). Sem ela a saída do Ansible vem em texto e o NiktoService não
        // consegue extrair o resultado. `env VAR=valor comando` roda o ansible já com ela.
        return new String[]{
                "env", "ANSIBLE_STDOUT_CALLBACK=json",
                "ansible-playbook",
                "-i", alvo.getIp() + ",",
                playbook
        };
    }
}
    

