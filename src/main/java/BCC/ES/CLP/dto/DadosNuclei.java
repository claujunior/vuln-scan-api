package BCC.ES.CLP.dto;

import java.util.List;

public record DadosNuclei(String ip, List<VulnerabilidadeResumo> vulnerabilidades, String saidaBruta) implements DadosScan {

    // Sobrescrito porque o comportamento antigo (antes do refactor) retornava
    // direto a mensagem "Nenhuma vulnerabilidade..." sem chamar a LLM quando a
    // lista vinha vazia. Mantemos esse atalho aqui para não gastar quota da
    // LLM à toa quando o formato pedido for RELATORIO_LLM.
    @Override
    public boolean vazio() {
        return vulnerabilidades.isEmpty();
    }

    @Override
    public String mensagemVazia() {
        return "Nenhuma vulnerabilidade encontrada para " + ip;
    }

    @Override
    public String promptLlm() {
        StringBuilder resumo = new StringBuilder();
        for (VulnerabilidadeResumo v : vulnerabilidades) {
            resumo.append(v.templateId()).append(" [").append(v.severidade()).append("] em ")
                    .append(v.matchedAt()).append("; ");
        }
        return resumo + " faça um relatório sobre as vulnerabilidades encontradas e recomendações de correção";
    }
}
