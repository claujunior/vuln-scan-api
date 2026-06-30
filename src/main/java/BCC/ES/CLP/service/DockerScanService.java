package BCC.ES.CLP.service;
import org.springframework.stereotype.Component;

import BCC.ES.CLP.model.Alvo;
import BCC.ES.CLP.model.Select;

@Component("docker")
public class DockerScanService implements ExecutorStrategy {

    @Override
    public String[] montarComando(Alvo alvo, Select tool) {
        return new String[]{
                "docker", "run", "--rm", "--network", "host",
                "-v", tool.getInfra_Dir() + ":/app/infra",
                "scanner-image",
                "ansible-playbook",
                "-i", alvo.getIp() + ",",
                tool.getAnsible()
        };
    }
}
