package br.com.leitovivo.web.handler;

import br.com.leitovivo.exception.ConflitoNegocioException;
import br.com.leitovivo.exception.PayloadInvalidoException;
import br.com.leitovivo.exception.RecursoNaoEncontradoException;
import br.com.leitovivo.exception.TransicaoInvalidaException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> handleNotFound(RecursoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler({
            ConflitoNegocioException.class,
            TransicaoInvalidaException.class,
            OptimisticLockingFailureException.class
    })
    public ResponseEntity<ErroResponse> handleConflict(Exception ex) {
        String detail = ex.getMessage() != null ? ex.getMessage() : "Conflito de negócio";
        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro(HttpStatus.CONFLICT, detail));
    }

    @ExceptionHandler({PayloadInvalidoException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<ErroResponse> handleUnprocessable(Exception ex) {
        String detail = ex instanceof MethodArgumentNotValidException manv
                ? manv.getBindingResult().getFieldErrors().stream()
                    .findFirst()
                    .map(err -> err.getField() + ": " + err.getDefaultMessage())
                    .orElse("Payload inválido")
                : ex.getMessage();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(erro(HttpStatus.UNPROCESSABLE_ENTITY, detail));
    }

    private static ErroResponse erro(HttpStatus status, String detail) {
        return new ErroResponse(
                URI.create("about:blank"),
                status.getReasonPhrase(),
                status.value(),
                detail);
    }
}
