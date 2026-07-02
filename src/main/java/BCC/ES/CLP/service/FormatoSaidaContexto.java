package BCC.ES.CLP.service;

import BCC.ES.CLP.model.FormatoSaida;
import org.springframework.stereotype.Component;

/**
 * Carrega o {@link FormatoSaida} escolhido pelo usuario do inicio ao fim de uma
 * execucao de scan, sem alterar a assinatura fixa de
 * {@link ScanTemplate#executar} / {@code processar}, que nao recebem esse dado
 * como parametro.
 * <p>
 * Usa ThreadLocal porque ServiceNmap/ServiceNuclei sao singletons do Spring
 * compartilhados entre requisicoes concorrentes. Isso e seguro aqui porque
 * {@link ScanTemplate#executar} bloqueia (join) a thread chamadora ate o scan
 * terminar: a mesma thread HTTP que define o formato em
 * {@link ServiceScan#seletor} e a que executa {@code processar} depois.
 */
@Component
public class FormatoSaidaContexto {

    private final ThreadLocal<FormatoSaida> formato = new ThreadLocal<>();

    public void definir(FormatoSaida formatoSaida) {
        formato.set(formatoSaida);
    }

    public FormatoSaida obter() {
        FormatoSaida atual = formato.get();
        return atual != null ? atual : FormatoSaida.RELATORIO_LLM;
    }

    public void limpar() {
        formato.remove();
    }
}
