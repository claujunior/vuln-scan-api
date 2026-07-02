package BCC.ES.CLP.dto;

import java.util.Set;

// Dados estruturados do scan Nmap. Antes só existiam como texto embutido no
// prompt da LLM; agora viram um objeto próprio para poder ser devolvido como
// JSON puro (formato JSON) sem depender da LLM.
public record DadosNmap(String ip, Set<String> portas, String saidaBruta) implements DadosScan {

    @Override
    public String promptLlm() {
        return "{\"host\":\"" + ip + "\",\"portas\":\"" + portas + "\"}"
                + " faça um relatorio sobre as portas e os servicos e diga as vulnerabilidades de seguranca";
    }
}
