package br.com.leitovivo.persistence;

import br.com.leitovivo.domain.StatusLeito;
import br.com.leitovivo.domain.TipoLeito;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeitoAplicarTransicaoTest {

    @Test
    void aplicarTransicaoAtualizaStatusETimestampJuntos() {
        Unidade unidade = new Unidade("H", "SP", "SE", "Geral");
        Instant criado = Instant.parse("2026-07-13T10:00:00Z");
        Leito leito = new Leito(unidade, "L-1", TipoLeito.CLINICO, StatusLeito.LIVRE, false, criado);

        Instant agora = Instant.parse("2026-07-13T12:00:00Z");
        leito.aplicarTransicao(StatusLeito.RESERVADO, agora);

        assertEquals(StatusLeito.RESERVADO, leito.getStatus());
        assertEquals(agora, leito.getDataUltimaAtualizacaoStatus());
        assertTrue(Arrays.stream(Leito.class.getMethods()).noneMatch(m -> m.getName().equals("setStatus")));
    }
}
