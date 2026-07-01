package BCC.ES.CLP.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import BCC.ES.CLP.model.Alvo;
import BCC.ES.CLP.model.User;
import BCC.ES.CLP.service.ServiceInterface;

@RestController
@RequestMapping("/Alvo")
public class ControllerAlvo {


   private final ServiceInterface serviceInterface;

   public ControllerAlvo(ServiceInterface serviceInterface){
       this.serviceInterface=serviceInterface;
   }

   @GetMapping("/get")
   public ResponseEntity<List<Alvo>> obterAlvos(@AuthenticationPrincipal User dono){
        return ResponseEntity.ok(serviceInterface.allAlvos(dono));
   }

   @PostMapping("/post")
   public ResponseEntity<Alvo> cadastrarAlvos(@RequestBody Alvo alvo,
                                              @AuthenticationPrincipal User dono){
      serviceInterface.salvarAlvo(alvo, dono);
      return ResponseEntity.ok(alvo);
   }

   @PutMapping("/update")
   public ResponseEntity<Alvo> atualizarAlvo(@RequestBody Alvo alvo,
                                             @AuthenticationPrincipal User dono){
      return ResponseEntity.ok(serviceInterface.atualizarAlvo(alvo.getId(), alvo, dono));
   }

    @DeleteMapping("/delete/{id}")
      public ResponseEntity<Alvo> deleteAlvo(@PathVariable Long id,
                                             @AuthenticationPrincipal User dono){
        return ResponseEntity.ok(serviceInterface.deletarAlvo(id, dono));
    }
}
