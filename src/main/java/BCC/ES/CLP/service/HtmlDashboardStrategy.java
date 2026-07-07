package BCC.ES.CLP.service;

import BCC.ES.CLP.dto.VulnerabilidadeResumo;
import BCC.ES.CLP.model.FormatoSaida;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Gera um mini-dashboard em HTML autocontido (CSS inline, sem dependencias
 * externas) a partir dos dados ja estruturados do scan (portas ou
 * vulnerabilidades), sem passar pela LLM.
 *
 * Reconhece duas formas de "dadosEstruturados", que sao as mesmas produzidas
 * hoje por ServiceNmap (chave "portas") e ServiceNuclei (chave
 * "vulnerabilidades"). Se nenhuma delas for encontrada, cai num cartao
 * generico mostrando o que existir no mapa.
 */
@Service
public class HtmlDashboardStrategy implements SaidaStrategy {

    @Override
    public FormatoSaida formato() {
        return FormatoSaida.HTML;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String gerar(Object dadosEstruturados, String prompt, String saidaBruta) {
        Map<String, Object> dados = (dadosEstruturados instanceof Map)
                ? (Map<String, Object>) dadosEstruturados
                : Map.of();

        String ip = String.valueOf(dados.getOrDefault("ip", "desconhecido"));

        String corpo;
        if (dados.get("vulnerabilidades") instanceof List<?> lista) {
            corpo = montarDashboardVulnerabilidades(ip, (List<VulnerabilidadeResumo>) lista);
        } else if (dados.get("portas") instanceof Iterable<?> portas) {
            corpo = montarDashboardPortas(ip, portas);
        } else {
            corpo = "<div class=\"card\"><p>Sem dados estruturados reconhecidos para exibir.</p></div>";
        }

        return template(ip, corpo);
    }

    private String montarDashboardPortas(String ip, Iterable<?> portas) {
        StringBuilder linhas = new StringBuilder();
        int total = 0;
        for (Object porta : portas) {
            String[] partes = String.valueOf(porta).split(":", 2);
            String numero = partes[0];
            String servico = partes.length > 1 ? partes[1] : "desconhecido";
            linhas.append("<tr><td>").append(escapar(numero)).append("</td><td>")
                    .append(escapar(servico)).append("</td></tr>");
            total++;
        }

        return "<div class=\"summary\">"
                + card(String.valueOf(total), "Portas abertas", "info")
                + "</div>"
                + "<table><thead><tr><th>Porta</th><th>Serviço</th></tr></thead><tbody>"
                + (total == 0 ? "<tr><td colspan=\"2\">Nenhuma porta aberta encontrada.</td></tr>" : linhas)
                + "</tbody></table>";
    }

    private String montarDashboardVulnerabilidades(String ip, List<VulnerabilidadeResumo> vulnerabilidades) {
        long critical = contarSeveridade(vulnerabilidades, "critical");
        long high = contarSeveridade(vulnerabilidades, "high");
        long medium = contarSeveridade(vulnerabilidades, "medium");
        long low = contarSeveridade(vulnerabilidades, "low", "info");

        StringBuilder linhas = new StringBuilder();
        for (VulnerabilidadeResumo v : vulnerabilidades) {
            String sev = v.severidade() == null ? "unknown" : v.severidade().toLowerCase();
            linhas.append("<tr><td>").append(escapar(v.templateId())).append("</td><td>")
                    .append(escapar(v.nome())).append("</td><td>")
                    .append(badge(sev)).append("</td><td>")
                    .append(escapar(v.matchedAt())).append("</td></tr>");
        }

        return "<div class=\"summary\">"
                + card(String.valueOf(critical), "Críticas", "critical")
                + card(String.valueOf(high), "Altas", "high")
                + card(String.valueOf(medium), "Médias", "medium")
                + card(String.valueOf(low), "Baixas/Info", "low")
                + "</div>"
                + "<table><thead><tr><th>Template</th><th>Nome</th><th>Severidade</th><th>Encontrado em</th></tr></thead><tbody>"
                + (vulnerabilidades.isEmpty()
                        ? "<tr><td colspan=\"4\">Nenhuma vulnerabilidade encontrada.</td></tr>"
                        : linhas)
                + "</tbody></table>";
    }

    private long contarSeveridade(List<VulnerabilidadeResumo> vulnerabilidades, String... severidades) {
        return vulnerabilidades.stream()
                .filter(v -> v.severidade() != null)
                .filter(v -> {
                    String sev = v.severidade().toLowerCase();
                    for (String alvo : severidades) {
                        if (sev.equals(alvo)) return true;
                    }
                    return false;
                })
                .count();
    }

    private String card(String valor, String rotulo, String classe) {
        return "<div class=\"card card-" + classe + "\"><span class=\"card-valor\">" + valor
                + "</span><span class=\"card-rotulo\">" + rotulo + "</span></div>";
    }

    private String badge(String severidade) {
        return "<span class=\"badge badge-" + escapar(severidade) + "\">" + escapar(severidade) + "</span>";
    }

    private String escapar(String texto) {
        if (texto == null) return "";
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String template(String ip, String corpo) {
        return """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head>
                <meta charset="UTF-8">
                <title>Dashboard de Scan - %s</title>
                <style>
                  :root {
                    --bg: #0f172a;
                    --card-bg: #1e293b;
                    --border: #334155;
                    --text: #e2e8f0;
                    --muted: #94a3b8;
                    --accent: #38bdf8;
                  }
                  * { box-sizing: border-box; }
                  body {
                    margin: 0;
                    padding: 32px;
                    background: var(--bg);
                    color: var(--text);
                    font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
                  }
                  .header {
                    display: flex;
                    align-items: baseline;
                    gap: 12px;
                    margin-bottom: 24px;
                    border-bottom: 1px solid var(--border);
                    padding-bottom: 16px;
                  }
                  .header h1 { font-size: 20px; margin: 0; }
                  .header span { color: var(--muted); font-size: 14px; }
                  .summary { display: flex; gap: 16px; margin-bottom: 24px; flex-wrap: wrap; }
                  .card {
                    background: var(--card-bg);
                    border: 1px solid var(--border);
                    border-radius: 10px;
                    padding: 16px 20px;
                    min-width: 120px;
                    display: flex;
                    flex-direction: column;
                    gap: 4px;
                    border-left: 4px solid var(--accent);
                  }
                  .card-valor { font-size: 28px; font-weight: 600; }
                  .card-rotulo { font-size: 12px; color: var(--muted); text-transform: uppercase; letter-spacing: 0.04em; }
                  .card-critical { border-left-color: #f87171; }
                  .card-high { border-left-color: #fb923c; }
                  .card-medium { border-left-color: #facc15; }
                  .card-low { border-left-color: #4ade80; }
                  .card-info { border-left-color: #38bdf8; }
                  table {
                    width: 100%%;
                    border-collapse: collapse;
                    background: var(--card-bg);
                    border: 1px solid var(--border);
                    border-radius: 10px;
                    overflow: hidden;
                  }
                  th, td {
                    text-align: left;
                    padding: 10px 14px;
                    border-bottom: 1px solid var(--border);
                    font-size: 14px;
                  }
                  th { color: var(--muted); font-weight: 500; text-transform: uppercase; font-size: 11px; letter-spacing: 0.04em; }
                  tbody tr:hover { background: rgba(148, 163, 184, 0.08); }
                  tbody tr:last-child td { border-bottom: none; }
                  .badge {
                    padding: 3px 10px;
                    border-radius: 999px;
                    font-size: 12px;
                    font-weight: 600;
                    text-transform: capitalize;
                  }
                  .badge-critical { background: rgba(248, 113, 113, 0.15); color: #f87171; }
                  .badge-high { background: rgba(251, 146, 60, 0.15); color: #fb923c; }
                  .badge-medium { background: rgba(250, 204, 21, 0.15); color: #facc15; }
                  .badge-low, .badge-info { background: rgba(74, 222, 128, 0.15); color: #4ade80; }
                  .badge-unknown { background: rgba(148, 163, 184, 0.15); color: #94a3b8; }
                </style>
                </head>
                <body>
                <div class="header">
                  <h1>Dashboard de Scan</h1>
                  <span>Alvo: %s</span>
                </div>
                %s
                </body>
                </html>
                """.formatted(ip, ip, corpo);
    }
}
