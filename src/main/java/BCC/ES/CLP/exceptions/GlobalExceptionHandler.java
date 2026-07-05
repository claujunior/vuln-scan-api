package BCC.ES.CLP.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 404 NOT_FOUND            - Recurso não encontrado 
 * 409 CONFLICT             - Conflito de dados 
 * 500 INTERNAL_SERVER_ERROR- Erro interno do servidor 
 * 400 BAD_REQUEST          - Requisição inválida 
 * 401 UNAUTHORIZED         - Falha de autenticação 
 * 504 GATEWAY_TIMEOUT      - Timeout em operações de scan
 */

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AlvoNaoEncontradoException.class)
    public ResponseEntity<String> handleAlvoNaoEncontrado(AlvoNaoEncontradoException ex) {
    return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ex.getMessage());
    }
    
    @ExceptionHandler(AlvoJaRegistradoException.class)
    public ResponseEntity<String> handleAlvoJaRegistrado(AlvoJaRegistradoException ex) {
    return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
    return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body("Conflito/violação de integridade no banco (duplicado ou referência inválida).");
    }

    @ExceptionHandler(ScanOrquestracaoException.class)
    public ResponseEntity<String> handleScanOrquestracao(ScanOrquestracaoException ex) {
    return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ex.getMessage());
    }

    @ExceptionHandler(PortasFechadas.class)
    public ResponseEntity<String> handlePortasFechadas(PortasFechadas ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }
    @ExceptionHandler(ErroAoEncontrarIp.class)
    public ResponseEntity<String> handleErroAoEncontrarIp(ErroAoEncontrarIp ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }
    @ExceptionHandler(TimeoutScan.class)
    public ResponseEntity<String> handleTimeoutScan(TimeoutScan ex) {
        return ResponseEntity
                .status(HttpStatus.GATEWAY_TIMEOUT)
                .body(ex.getMessage());
    }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleAuthError(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Credenciais inválidas");
    }
    @ExceptionHandler(UsuarioJaExistente.class)
    public ResponseEntity<String> handleAuthError(UsuarioJaExistente e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(e.getMessage());
    }
    @ExceptionHandler(IpJaNaoExistente.class)
    public ResponseEntity<String> handleIpJaNaoExistente(IpJaNaoExistente e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }
    @ExceptionHandler(UrlJaNaoExistente.class)
    public ResponseEntity<String> handleIpJaNaoExistente(UrlJaNaoExistente e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }
    @ExceptionHandler(TokenException.class)
    public ResponseEntity<String> handleTokenException(TokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }
    @ExceptionHandler(AlvoInvalido.class)
    public ResponseEntity<String> handleAlvoInvalido(AlvoInvalido ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
    
    @ExceptionHandler(UrlInvalida.class)
    public ResponseEntity<String> handleUrlInvalida(UrlInvalida ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(MonitoramentoNaoEncontrado.class)
    public ResponseEntity<String> handleMonitoramentoNaoEncontrado(MonitoramentoNaoEncontrado ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(NotificacaoNaoEncontrada.class)
    public ResponseEntity<String> handleNotificacaoNaoEncontrada(NotificacaoNaoEncontrada ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
