package BCC.ES.CLP.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import BCC.ES.CLP.exceptions.NotificacaoNaoEncontrada;
import BCC.ES.CLP.model.Notificacao;
import BCC.ES.CLP.repository.RepositoryNotificacao;

@Service
public class ServiceNotificacao {

    private final RepositoryNotificacao repositoryNotificacao;

    public ServiceNotificacao(RepositoryNotificacao repositoryNotificacao) {
        this.repositoryNotificacao = repositoryNotificacao;
    }

    @Transactional(readOnly = true)
    public List<Notificacao> listar() {
        return repositoryNotificacao.findAllByOrderByDataHoraDesc();
    }

    @Transactional
    public void marcarLida(Long id) {
        Notificacao notificacao = repositoryNotificacao.findById(id)
                .orElseThrow(NotificacaoNaoEncontrada::new);
        notificacao.setLida(true);
        repositoryNotificacao.save(notificacao);
    }

    @Transactional
    public void remover(Long id) {
        Notificacao notificacao = repositoryNotificacao.findById(id)
                .orElseThrow(NotificacaoNaoEncontrada::new);
        repositoryNotificacao.delete(notificacao);
    }
}
