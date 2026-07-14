package br.com.leitovivo.persistence;

import br.com.leitovivo.persistence.entity.AlertaLeito;
import br.com.leitovivo.persistence.entity.SlaStatusLeito;

import br.com.leitovivo.domain.leito.enums.StatusLeito;
import br.com.leitovivo.domain.leito.enums.TipoLeito;
import br.com.leitovivo.domain.sla.enums.AcaoAutomatica;
import br.com.leitovivo.domain.sla.enums.SituacaoAlerta;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SlaAlertaMappingContractTest {

    @Test
    void slaStatusLeitoMapeiaUuidEnumsECuringas() throws Exception {
        Field id = SlaStatusLeito.class.getDeclaredField("id");
        assertNotNull(id.getAnnotation(Id.class));

        Field status = SlaStatusLeito.class.getDeclaredField("status");
        assertEquals(EnumType.STRING, status.getAnnotation(Enumerated.class).value());
        assertEquals(StatusLeito.class, status.getType());

        Field tipo = SlaStatusLeito.class.getDeclaredField("tipoLeito");
        assertEquals(EnumType.STRING, tipo.getAnnotation(Enumerated.class).value());
        assertEquals(TipoLeito.class, tipo.getType());

        Field acao = SlaStatusLeito.class.getDeclaredField("acaoAutomatica");
        assertEquals(EnumType.STRING, acao.getAnnotation(Enumerated.class).value());
        assertEquals(AcaoAutomatica.class, acao.getType());
    }

    @Test
    void alertaLeitoMapeiaSituacaoEStatusEmAlertaComoString() throws Exception {
        Field id = AlertaLeito.class.getDeclaredField("id");
        assertNotNull(id.getAnnotation(Id.class));

        Field situacao = AlertaLeito.class.getDeclaredField("situacao");
        assertEquals(EnumType.STRING, situacao.getAnnotation(Enumerated.class).value());
        assertEquals(SituacaoAlerta.class, situacao.getType());

        Field status = AlertaLeito.class.getDeclaredField("statusEmAlerta");
        assertEquals(EnumType.STRING, status.getAnnotation(Enumerated.class).value());
        assertEquals(StatusLeito.class, status.getType());
    }
}
