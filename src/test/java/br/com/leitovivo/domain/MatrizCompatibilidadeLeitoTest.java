package br.com.leitovivo.domain;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatrizCompatibilidadeLeitoTest {

    @Test
    void necessidadeUtiNaoAceitaEnfermariaNemClinico() {
        Set<TipoLeito> compativeis = MatrizCompatibilidadeLeito.tiposCompativeis(TipoLeito.UTI);

        assertTrue(compativeis.contains(TipoLeito.UTI));
        assertFalse(compativeis.contains(TipoLeito.ENFERMARIA));
        assertFalse(compativeis.contains(TipoLeito.CLINICO));
        assertEquals(Set.of(TipoLeito.UTI), compativeis);
    }

    @Test
    void necessidadeEnfermariaAceitaMaiorComplexidade() {
        Set<TipoLeito> compativeis = MatrizCompatibilidadeLeito.tiposCompativeis(TipoLeito.ENFERMARIA);

        assertTrue(compativeis.contains(TipoLeito.ENFERMARIA));
        assertTrue(compativeis.contains(TipoLeito.CLINICO));
        assertTrue(compativeis.contains(TipoLeito.UTI));
        assertEquals(Set.of(TipoLeito.ENFERMARIA, TipoLeito.CLINICO, TipoLeito.UTI), compativeis);
    }

    @Test
    void necessidadeNeonatalNaoEAtendidaPorUtiAdulta() {
        Set<TipoLeito> compativeis = MatrizCompatibilidadeLeito.tiposCompativeis(TipoLeito.UTI_NEONATAL);

        assertEquals(Set.of(TipoLeito.UTI_NEONATAL), compativeis);
        assertFalse(MatrizCompatibilidadeLeito.atende(TipoLeito.UTI_NEONATAL, TipoLeito.UTI));
    }
}
