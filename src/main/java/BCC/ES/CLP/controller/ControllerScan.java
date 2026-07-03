package BCC.ES.CLP.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import BCC.ES.CLP.model.FormatoSaida;
import BCC.ES.CLP.model.Scan;
import BCC.ES.CLP.model.Select;
import BCC.ES.CLP.model.User;
import BCC.ES.CLP.service.ServiceScan;

@RestController
@RequestMapping("/Scan")
public class ControllerScan {




    private final ServiceScan serviceScan;

    public ControllerScan(ServiceScan serviceScan){
        this.serviceScan=serviceScan;
    }

    @GetMapping("/get")
    public ResponseEntity<List<Scan>> listarScans(@AuthenticationPrincipal User dono) {
        return ResponseEntity.ok(serviceScan.allScan(dono));
    }


    @PostMapping("/post/{id}/{tipo}/{executor}/{formato}")
    public ResponseEntity<String> executar(@PathVariable Long id, @PathVariable String tipo,
                                           @PathVariable String executor, @PathVariable String formato,
                                           @AuthenticationPrincipal User dono) {
        Select select = Select.valueOf(tipo.toUpperCase());
        FormatoSaida formatoSaida = FormatoSaida.valueOf(formato.toUpperCase());
        return ResponseEntity.ok(serviceScan.seletor(id, select, executor, formatoSaida, dono));
    }

}
