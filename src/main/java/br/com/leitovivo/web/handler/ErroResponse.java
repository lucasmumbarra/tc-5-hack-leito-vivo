package br.com.leitovivo.web.handler;

import java.net.URI;

/**
 * Corpo de erro HTTP equivalente ao {@code ProblemDetail} usado anteriormente
 * (campos serializados: type, title, status, detail).
 */
public record ErroResponse(
        URI type,
        String title,
        int status,
        String detail) {
}
