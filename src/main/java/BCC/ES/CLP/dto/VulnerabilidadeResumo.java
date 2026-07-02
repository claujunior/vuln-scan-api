package BCC.ES.CLP.dto;

// DTO enxuto para expor as vulnerabilidades na saída JSON, em vez de serializar
// a entidade JPA Vulnerabilidade direto (evita vazar detalhes de persistência,
// como o relacionamento com Alvo, na resposta da API).
public record VulnerabilidadeResumo(String templateId, String nome, String severidade, String matchedAt) {}
