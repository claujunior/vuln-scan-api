package BCC.ES.CLP.dto;

import java.util.List;

// Dados estruturados do scan Nikto. Mesma forma do DadosNuclei (lista de
// vulnerabilidades), mas mantido como tipo próprio para seguir o padrão
// por-ferramenta e permitir divergir no futuro (o Nikto, por exemplo, não
// fornece severidade, então aqui ela vem sempre como "info").
public record DadosNikto(String ip, List<VulnerabilidadeResumo> vulnerabilidades, String saidaBruta) implements DadosScan {

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
                    .append(v.matchedAt()).append(" - ").append(v.nome()).append("; ");
        }
        return resumo + " faça um relatório sobre as vulnerabilidades encontradas e recomendações de correção";
    }
}
