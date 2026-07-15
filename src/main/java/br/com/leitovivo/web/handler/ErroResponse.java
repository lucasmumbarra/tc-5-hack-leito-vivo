package br.com.leitovivo.web.handler;

import java.net.URI;

public record ErroResponse(
    URI type,
    String title,
    int status,
    String detail) {
}
