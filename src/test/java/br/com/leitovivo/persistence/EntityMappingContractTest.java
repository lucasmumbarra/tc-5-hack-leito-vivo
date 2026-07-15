package br.com.leitovivo.persistence;

import br.com.leitovivo.domain.leito.enums.StatusLeito;
import br.com.leitovivo.domain.leito.enums.TipoLeito;
import br.com.leitovivo.persistence.entity.Leito;
import br.com.leitovivo.persistence.entity.Paciente;
import br.com.leitovivo.persistence.entity.Unidade;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityMappingContractTest {

  @Test
  void leitoMapeiaUuidEnumsVersionEUnique() throws Exception {
    Field id = Leito.class.getDeclaredField("id");
    assertNotNull(id.getAnnotation(Id.class));
    assertEquals(GenerationType.UUID, id.getAnnotation(GeneratedValue.class).strategy());

    Field status = Leito.class.getDeclaredField("status");
    assertEquals(EnumType.STRING, status.getAnnotation(Enumerated.class).value());
    assertEquals(StatusLeito.class, status.getType());

    Field tipo = Leito.class.getDeclaredField("tipo");
    assertEquals(EnumType.STRING, tipo.getAnnotation(Enumerated.class).value());
    assertEquals(TipoLeito.class, tipo.getType());

    assertNotNull(Leito.class.getDeclaredField("versao").getAnnotation(Version.class));

    UniqueConstraint[] uniques = Leito.class.getAnnotation(Table.class).uniqueConstraints();
    assertEquals(1, uniques.length);
    assertEquals("uq_leito_unidade_codigo", uniques[0].name());
  }

  @Test
  void entidadesNaoUsamOrdinalNemIdentity() throws Exception {
    for (Class<?> type : new Class<?>[] {Unidade.class, Paciente.class, Leito.class}) {
      Field id = type.getDeclaredField("id");
      assertEquals(GenerationType.UUID, id.getAnnotation(GeneratedValue.class).strategy());
    }
    Field status = Leito.class.getDeclaredField("status");
    assertFalse(status.getAnnotation(Enumerated.class).value() == EnumType.ORDINAL);
    assertTrue(status.getAnnotation(Enumerated.class).value() == EnumType.STRING);
  }
}
