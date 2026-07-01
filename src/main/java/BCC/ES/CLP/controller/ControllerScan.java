package BCC.ES.CLP.controller;

import java.util.List;

import BCC.ES.CLP.model.Select;
import BCC.ES.CLP.model.User;
import BCC.ES.CLP.service.ServiceOrquestrador;
import BCC.ES.CLP.service.ServiceScan;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import BCC.ES.CLP.model.Scan;

@RestController
@RequestMapping("/Scan")
public class ControllerScan {


    private final ServiceOrquestrador serviceOrquestrador;

    private final ServiceScan serviceScan;

    public ControllerScan(ServiceScan serviceScan,ServiceOrquestrador serviceOrquestrador){
        this.serviceScan=serviceScan;
        this.serviceOrquestrador=serviceOrquestrador;
    }

    @GetMapping("/get")
    public ResponseEntity<List<Scan>> listarScans(@AuthenticationPrincipal User dono) {
        return ResponseEntity.ok(serviceScan.allScan(dono));
    }

    @PostMapping("/post/{id}/{tipo}/{executor}")
    public ResponseEntity<String> executar(@PathVariable Long id, @PathVariable String tipo,
                                           @PathVariable String executor,
                                           @AuthenticationPrincipal User dono) {
        Select select = Select.valueOf(tipo.toUpperCase());
        return ResponseEntity.ok(serviceScan.seletor(id, select, executor, dono));
    }

}
