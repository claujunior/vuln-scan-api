package BCC.ES.CLP.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import BCC.ES.CLP.model.Notificacao;
import BCC.ES.CLP.service.ServiceNotificacao;

@RestController
@RequestMapping("/Notificacao")
public class ControllerNotificacao {

    private final ServiceNotificacao serviceNotificacao;

    public ControllerNotificacao(ServiceNotificacao serviceNotificacao) {
        this.serviceNotificacao = serviceNotificacao;
    }

    @GetMapping("/get")
    public ResponseEntity<List<Notificacao>> listar() {
        return ResponseEntity.ok(serviceNotificacao.listar());
    }

    @PutMapping("/lida/{id}")
    public ResponseEntity<Void> marcarLida(@PathVariable Long id) {
        serviceNotificacao.marcarLida(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        serviceNotificacao.remover(id);
        return ResponseEntity.ok().build();
    }
}
