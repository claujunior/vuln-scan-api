package BCC.ES.CLP.service;

import BCC.ES.CLP.model.Alvo;

/**
 * Estrategia de entrega de notificacoes quando o monitoramento continuo
 * encontra um problema em um alvo. Cada implementacao representa um canal
 * (log, webhook, e-mail, etc). Para adicionar um novo canal: criar uma
 * classe @Component(nome-do-canal) implementando esta interface — o nome do
 * bean deve bater com o campo Monitoramento.canalNotificacao escolhido pelo
 * usuario.
 */
public interface NotificadorStrategy {

    String canal();

    void enviar(Alvo alvo, String titulo, String mensagem);
}
