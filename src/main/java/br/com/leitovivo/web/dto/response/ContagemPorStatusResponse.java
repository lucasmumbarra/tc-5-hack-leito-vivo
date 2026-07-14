package br.com.leitovivo.web.dto.response;

public record ContagemPorStatusResponse(
        long livre,
        long reservado,
        long ocupado,
        long emHigienizacao,
        long manutencao) {
}
