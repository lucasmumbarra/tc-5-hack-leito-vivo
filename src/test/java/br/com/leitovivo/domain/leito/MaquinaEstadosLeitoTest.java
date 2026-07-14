package br.com.leitovivo.domain.leito;

import br.com.leitovivo.domain.leito.enums.Autor;
import br.com.leitovivo.domain.leito.enums.EventoLeito;
import br.com.leitovivo.domain.leito.enums.StatusLeito;
import br.com.leitovivo.exception.TransicaoInvalidaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaquinaEstadosLeitoTest {

    private static final Map<StatusEvento, StatusLeito> TRANSICOES_VALIDAS = Map.ofEntries(
            Map.entry(new StatusEvento(StatusLeito.LIVRE, EventoLeito.RESERVAR_LEITO), StatusLeito.RESERVADO),
            Map.entry(new StatusEvento(StatusLeito.LIVRE, EventoLeito.INTERNAR_PACIENTE), StatusLeito.OCUPADO),
            Map.entry(new StatusEvento(StatusLeito.LIVRE, EventoLeito.ENVIAR_MANUTENCAO), StatusLeito.MANUTENCAO),
            Map.entry(new StatusEvento(StatusLeito.RESERVADO, EventoLeito.INTERNAR_PACIENTE), StatusLeito.OCUPADO),
            Map.entry(new StatusEvento(StatusLeito.RESERVADO, EventoLeito.CANCELAR_RESERVA), StatusLeito.LIVRE),
            Map.entry(new StatusEvento(StatusLeito.RESERVADO, EventoLeito.ENVIAR_MANUTENCAO), StatusLeito.MANUTENCAO),
            Map.entry(new StatusEvento(StatusLeito.OCUPADO, EventoLeito.REGISTRAR_ALTA), StatusLeito.EM_HIGIENIZACAO),
            Map.entry(new StatusEvento(StatusLeito.EM_HIGIENIZACAO, EventoLeito.FINALIZAR_HIGIENIZACAO), StatusLeito.LIVRE),
            Map.entry(new StatusEvento(StatusLeito.EM_HIGIENIZACAO, EventoLeito.ENVIAR_MANUTENCAO), StatusLeito.MANUTENCAO),
            Map.entry(new StatusEvento(StatusLeito.MANUTENCAO, EventoLeito.FINALIZAR_MANUTENCAO), StatusLeito.LIVRE)
    );

    @Test
    void internacaoEmLeitoLivre() {
        assertEquals(
                StatusLeito.OCUPADO,
                MaquinaEstadosLeito.transicionar(StatusLeito.LIVRE, EventoLeito.INTERNAR_PACIENTE));
    }

    @Test
    void internacaoEmLeitoOcupadoRejeitada() {
        assertThrows(
                TransicaoInvalidaException.class,
                () -> MaquinaEstadosLeito.transicionar(StatusLeito.OCUPADO, EventoLeito.INTERNAR_PACIENTE));
    }

    @Test
    void liberacaoSemHigienizacaoRejeitada() {
        assertThrows(
                TransicaoInvalidaException.class,
                () -> MaquinaEstadosLeito.transicionar(StatusLeito.OCUPADO, EventoLeito.FINALIZAR_HIGIENIZACAO));
    }

    @Test
    void leitoEmManutencaoIndisponivel() {
        assertThrows(
                TransicaoInvalidaException.class,
                () -> MaquinaEstadosLeito.transicionar(StatusLeito.MANUTENCAO, EventoLeito.INTERNAR_PACIENTE));
        assertThrows(
                TransicaoInvalidaException.class,
                () -> MaquinaEstadosLeito.transicionar(StatusLeito.MANUTENCAO, EventoLeito.RESERVAR_LEITO));
    }

    @Test
    void finalizarHigienizacaoNaoDependeDeAtor() {
        assertEquals(
                StatusLeito.LIVRE,
                MaquinaEstadosLeito.transicionar(StatusLeito.EM_HIGIENIZACAO, EventoLeito.FINALIZAR_HIGIENIZACAO));

        Method metodo = Arrays.stream(MaquinaEstadosLeito.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("transicionar"))
                .findFirst()
                .orElseThrow();
        assertEquals(2, metodo.getParameterCount());
        assertTrue(Arrays.stream(metodo.getParameterTypes()).noneMatch(t -> t == Autor.class));
    }

    @ParameterizedTest
    @MethodSource("todasCombinacoes")
    void coberturaExaustivaStatusPorEvento(StatusLeito status, EventoLeito evento) {
        StatusEvento chave = new StatusEvento(status, evento);
        if (TRANSICOES_VALIDAS.containsKey(chave)) {
            assertEquals(TRANSICOES_VALIDAS.get(chave), MaquinaEstadosLeito.transicionar(status, evento));
        } else {
            assertThrows(
                    TransicaoInvalidaException.class,
                    () -> MaquinaEstadosLeito.transicionar(status, evento));
        }
    }

    @Test
    void exatamenteDezTransicoesSaoValidas() {
        long aceitas = Arrays.stream(StatusLeito.values())
                .flatMap(status -> Arrays.stream(EventoLeito.values())
                        .filter(evento -> {
                            try {
                                MaquinaEstadosLeito.transicionar(status, evento);
                                return true;
                            } catch (TransicaoInvalidaException ex) {
                                return false;
                            }
                        }))
                .count();
        assertEquals(10, aceitas);
        assertEquals(10, TRANSICOES_VALIDAS.size());
    }

    static Stream<Arguments> todasCombinacoes() {
        return Arrays.stream(StatusLeito.values())
                .flatMap(status -> Arrays.stream(EventoLeito.values())
                        .map(evento -> Arguments.of(status, evento)));
    }

    private record StatusEvento(StatusLeito status, EventoLeito evento) {
    }
}
