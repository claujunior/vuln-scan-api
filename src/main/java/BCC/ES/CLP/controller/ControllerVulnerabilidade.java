package BCC.ES.CLP.controller;

import BCC.ES.CLP.model.User;
import BCC.ES.CLP.model.Vulnerabilidade;
import BCC.ES.CLP.service.ServiceScan;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/Vulnerabilidade")
public class ControllerVulnerabilidade {

    private final ServiceScan serviceScan;

    public ControllerVulnerabilidade(ServiceScan serviceScan) {
        this.serviceScan = serviceScan;
    }

    @GetMapping("/get")
    public ResponseEntity<List<Vulnerabilidade>> listar(@AuthenticationPrincipal User dono) {
        return ResponseEntity.ok(serviceScan.allVulnerabilidades(dono));
    }
}
