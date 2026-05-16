package BCC.ES.CLP.controller;



import java.util.List;


import BCC.ES.CLP.service.ServiceOrquestrador;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import BCC.ES.CLP.model.Alvo;
import BCC.ES.CLP.service.ServiceInterface;

@RestController
@RequestMapping("/Alvo")
public class ControllerAlvo {


   private final ServiceInterface serviceInterface;

   public ControllerAlvo(ServiceInterface serviceInterface){
       this.serviceInterface=serviceInterface;
   }

   @GetMapping("/get")
   public ResponseEntity<List<Alvo>> obterAlvos(){
        return ResponseEntity.ok(serviceInterface.allAlvos());
   }

   @PostMapping("/post")
   public ResponseEntity<Alvo> cadastrarAlvos(@RequestBody Alvo alvo){
      serviceInterface.salvarAlvo(alvo);
      return ResponseEntity.ok(alvo);
   }


    @DeleteMapping("/delete/{id}")
      public ResponseEntity<Alvo> deleteAlvo(@PathVariable Long id){
        return ResponseEntity.ok(serviceInterface.deletarAlvo(id));
    }
}
