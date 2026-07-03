package BCC.ES.CLP.service;

import BCC.ES.CLP.dto.ScanRawResult;
import BCC.ES.CLP.model.FormatoSaida;
import BCC.ES.CLP.model.Select;
import BCC.ES.CLP.model.User;

public abstract class ScanTemplate {

      protected final ServiceOrquestrador serviceOrquestrador;

      protected ScanTemplate(ServiceOrquestrador serviceOrquestrador) {
            this.serviceOrquestrador = serviceOrquestrador;
      }

      /**
       * Metodo template: define o esqueleto do algoritmo.
       * O passo de execucao do scan (ponto fixo) e o mesmo para qualquer tipo de scan,
       * por isso o metodo e "final" e nao pode ser sobrescrito pelas subclasses.
       */
      public String executar(Long id, Select select, String executor, FormatoSaida formato, User dono) {
            ScanRawResult resultado = serviceOrquestrador.executarScan(id, select, executor, dono).join();
            return processar(resultado);
      }

      /**
       * Passo variavel do algoritmo: cada subclasse (Nmap, Nuclei, ...)
       * implementa a sua propria forma de processar o resultado do scan.
       */
      protected abstract String processar(ScanRawResult resultado);
}
