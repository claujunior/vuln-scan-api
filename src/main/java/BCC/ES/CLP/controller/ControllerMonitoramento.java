package BCC.ES.CLP.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import BCC.ES.CLP.dto.MonitoramentoDto;
import BCC.ES.CLP.model.Monitoramento;
import BCC.ES.CLP.service.ServiceMonitoramento;

@RestController
@RequestMapping("/Monitoramento")
public class ControllerMonitoramento {

    private final ServiceMonitoramento serviceMonitoramento;

    public ControllerMonitoramento(ServiceMonitoramento serviceMonitoramento) {
        this.serviceMonitoramento = serviceMonitoramento;
    }

    @GetMapping("/get")
    public ResponseEntity<List<Monitoramento>> listar() {
        return ResponseEntity.ok(serviceMonitoramento.listar());
    }

    @PostMapping("/post/{alvoId}")
    public ResponseEntity<Monitoramento> configurar(@PathVariable Long alvoId, @RequestBody MonitoramentoDto dto) {
        return ResponseEntity.ok(serviceMonitoramento.configurar(alvoId, dto));
    }

    @PutMapping("/toggle/{alvoId}/{ativo}")
    public ResponseEntity<Void> alternar(@PathVariable Long alvoId, @PathVariable boolean ativo) {
        serviceMonitoramento.alternarAtivo(alvoId, ativo);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/executar/{alvoId}")
    public ResponseEntity<Void> executarAgora(@PathVariable Long alvoId) {
        serviceMonitoramento.executarVerificacaoPorAlvo(alvoId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{alvoId}")
    public ResponseEntity<Void> remover(@PathVariable Long alvoId) {
        serviceMonitoramento.remover(alvoId);
        return ResponseEntity.ok().build();
    }
}
