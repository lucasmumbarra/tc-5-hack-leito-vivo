package br.com.leitovivo.domain;

import br.com.leitovivo.exception.TransicaoInvalidaException;

public final class MaquinaEstadosLeito {

    private MaquinaEstadosLeito() {
    }

    public static StatusLeito transicionar(StatusLeito atual, EventoLeito evento) {
        return switch (atual) {
            case LIVRE -> switch (evento) {
                case RESERVAR_LEITO -> StatusLeito.RESERVADO;
                case INTERNAR_PACIENTE -> StatusLeito.OCUPADO;
                case ENVIAR_MANUTENCAO -> StatusLeito.MANUTENCAO;
                case CANCELAR_RESERVA, REGISTRAR_ALTA, FINALIZAR_HIGIENIZACAO, FINALIZAR_MANUTENCAO
                        -> throw new TransicaoInvalidaException(atual, evento);
            };
            case RESERVADO -> switch (evento) {
                case INTERNAR_PACIENTE -> StatusLeito.OCUPADO;
                case CANCELAR_RESERVA -> StatusLeito.LIVRE;
                case ENVIAR_MANUTENCAO -> StatusLeito.MANUTENCAO;
                case RESERVAR_LEITO, REGISTRAR_ALTA, FINALIZAR_HIGIENIZACAO, FINALIZAR_MANUTENCAO
                        -> throw new TransicaoInvalidaException(atual, evento);
            };
            case OCUPADO -> switch (evento) {
                case REGISTRAR_ALTA -> StatusLeito.EM_HIGIENIZACAO;
                case RESERVAR_LEITO, CANCELAR_RESERVA, INTERNAR_PACIENTE,
                     FINALIZAR_HIGIENIZACAO, ENVIAR_MANUTENCAO, FINALIZAR_MANUTENCAO
                        -> throw new TransicaoInvalidaException(atual, evento);
            };
            case EM_HIGIENIZACAO -> switch (evento) {
                case FINALIZAR_HIGIENIZACAO -> StatusLeito.LIVRE;
                case ENVIAR_MANUTENCAO -> StatusLeito.MANUTENCAO;
                case RESERVAR_LEITO, CANCELAR_RESERVA, INTERNAR_PACIENTE,
                     REGISTRAR_ALTA, FINALIZAR_MANUTENCAO
                        -> throw new TransicaoInvalidaException(atual, evento);
            };
            case MANUTENCAO -> switch (evento) {
                case FINALIZAR_MANUTENCAO -> StatusLeito.LIVRE;
                case RESERVAR_LEITO, CANCELAR_RESERVA, INTERNAR_PACIENTE,
                     REGISTRAR_ALTA, FINALIZAR_HIGIENIZACAO, ENVIAR_MANUTENCAO
                        -> throw new TransicaoInvalidaException(atual, evento);
            };
        };
    }
}
