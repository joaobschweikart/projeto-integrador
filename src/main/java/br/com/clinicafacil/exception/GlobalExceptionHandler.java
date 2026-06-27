package br.com.clinicafacil.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler global de exceções da API REST.
 *
 * Captura exceções lançadas pelos Services e Controllers e retorna
 * respostas JSON padronizadas, seguindo boas práticas de API REST.
 * Conforme o princípio de código limpo e organização em camadas
 * orientado na disciplina.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ────────────────────────────────────────────────
    // Exceções de negócio customizadas
    // ────────────────────────────────────────────────

    /** Recurso não encontrado no banco de dados. */
    public static class RecursoNaoEncontradoException extends RuntimeException {
        public RecursoNaoEncontradoException(String mensagem) {
            super(mensagem);
        }
    }

    /** Violação de regra de negócio (ex.: conflito de horário, CPF duplicado). */
    public static class RegraDeNegocioException extends RuntimeException {
        public RegraDeNegocioException(String mensagem) {
            super(mensagem);
        }
    }

    // ────────────────────────────────────────────────
    // Handlers
    // ────────────────────────────────────────────────

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> handleNaoEncontrado(RecursoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErroResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroResponse> handleRegraDeNegocio(RegraDeNegocioException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErroResponse(HttpStatus.CONFLICT.value(), ex.getMessage()));
    }

    /**
     * Trata falhas de validação dos DTOs (@Valid, @NotBlank, @Email...).
     * Retorna mapa campo → mensagem de erro.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            campos.put(error.getField(), error.getDefaultMessage());
        }
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("erro", "Dados inválidos");
        body.put("campos", campos);
        body.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleGenerico(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErroResponse(500, "Erro interno: " + ex.getMessage()));
    }

    // ────────────────────────────────────────────────
    // DTO de erro padronizado
    // ────────────────────────────────────────────────

    public record ErroResponse(int status, String mensagem) {
        public String timestamp() {
            return LocalDateTime.now().toString();
        }
    }
}
