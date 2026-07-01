package BCC.ES.CLP.controller;

import java.util.List;

import BCC.ES.CLP.model.Select;
import BCC.ES.CLP.service.ServiceOrquestrador;
import BCC.ES.CLP.service.ServiceScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import BCC.ES.CLP.model.Alvo;
import BCC.ES.CLP.model.Scan;

@RestController
@RequestMapping("/Scan")
public class ControllerScan {




    private final ServiceScan serviceScan;

    public ControllerScan(ServiceScan serviceScan){
        this.serviceScan=serviceScan;
    }

    @GetMapping("/get")
    public ResponseEntity<List<Scan>> listarScans() {

        return ResponseEntity.ok(serviceScan.allScan());
    }

    @PostMapping("/post/{id}/{tipo}/{executor}")
    public ResponseEntity<String> executar(@PathVariable Long id, @PathVariable String tipo, @PathVariable String executor) {
        Select select = Select.valueOf(tipo.toUpperCase());
        return ResponseEntity.ok(serviceScan.seletor(id,select, executor));
    }

}