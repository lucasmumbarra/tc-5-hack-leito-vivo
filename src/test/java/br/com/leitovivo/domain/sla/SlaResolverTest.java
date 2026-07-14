package br.com.leitovivo.domain.sla;

import br.com.leitovivo.domain.StatusLeito;
import br.com.leitovivo.domain.TipoLeito;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlaResolverTest {

    private static final UUID UNIDADE_X = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID ID_ESPECIFICO = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ID_DEFAULT = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID ID_UNIDADE = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Test
    void slaEspecificoPrevalece() {
        SlaRegra especifico = regra(ID_ESPECIFICO, UNIDADE_X, TipoLeito.UTI, StatusLeito.OCUPADO, 100);
        SlaRegra padrao = regra(ID_DEFAULT, null, null, StatusLeito.OCUPADO, 28800);

        Optional<SlaRegra> resultado = SlaResolver.resolver(
                UNIDADE_X, TipoLeito.UTI, StatusLeito.OCUPADO, List.of(padrao, especifico));

        assertTrue(resultado.isPresent());
        assertEquals(ID_ESPECIFICO, resultado.get().id());
        assertEquals(100, resultado.get().prazoAlertaMin());
    }

    @Test
    void quedaParaODefault() {
        SlaRegra padrao = regra(ID_DEFAULT, null, null, StatusLeito.OCUPADO, 28800);

        Optional<SlaRegra> resultado = SlaResolver.resolver(
                UNIDADE_X, TipoLeito.CLINICO, StatusLeito.OCUPADO, List.of(padrao));

        assertTrue(resultado.isPresent());
        assertEquals(ID_DEFAULT, resultado.get().id());
    }

    @Test
    void statusSemSlaConfiguradoRetornaEmpty() {
        SlaRegra ocupado = regra(ID_DEFAULT, null, null, StatusLeito.OCUPADO, 28800);

        Optional<SlaRegra> resultado = SlaResolver.resolver(
                UNIDADE_X, TipoLeito.UTI, StatusLeito.LIVRE, List.of(ocupado));

        assertTrue(resultado.isEmpty());
    }

    @Test
    void cascadeUnidadeSemTipoAntesDeTipoSemUnidade() {
        SlaRegra porUnidade = regra(ID_UNIDADE, UNIDADE_X, null, StatusLeito.RESERVADO, 100);
        SlaRegra porTipo = regra(ID_ESPECIFICO, null, TipoLeito.UTI, StatusLeito.RESERVADO, 200);
        SlaRegra padrao = regra(ID_DEFAULT, null, null, StatusLeito.RESERVADO, 360);

        Optional<SlaRegra> resultado = SlaResolver.resolver(
                UNIDADE_X, TipoLeito.UTI, StatusLeito.RESERVADO, List.of(padrao, porTipo, porUnidade));

        assertEquals(ID_UNIDADE, resultado.orElseThrow().id());
    }

    private static SlaRegra regra(
            UUID id, UUID unidadeId, TipoLeito tipo, StatusLeito status, int prazoAlerta) {
        return new SlaRegra(id, unidadeId, tipo, status, prazoAlerta, null, AcaoAutomaticaSla.NENHUMA);
    }
}
