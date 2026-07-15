package br.com.leitovivo.persistence;

import br.com.leitovivo.domain.leito.enums.Autor;
import br.com.leitovivo.domain.leito.enums.EventoLeito;
import br.com.leitovivo.domain.leito.enums.StatusLeito;
import br.com.leitovivo.persistence.entity.HistoricoStatusLeito;
import br.com.leitovivo.persistence.entity.Internacao;
import br.com.leitovivo.persistence.entity.Leito;
import br.com.leitovivo.persistence.enums.StatusInternacao;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CicloInternacaoMappingContractTest {

  @Test
  void internacaoEHistoricoMapeiamUuidEEnumsString() throws Exception {
    Field internacaoId = Internacao.class.getDeclaredField("id");
    assertNotNull(internacaoId.getAnnotation(Id.class));
    assertEquals(GenerationType.UUID, internacaoId.getAnnotation(GeneratedValue.class).strategy());

    Field status = Internacao.class.getDeclaredField("status");
    assertEquals(EnumType.STRING, status.getAnnotation(Enumerated.class).value());
    assertEquals(StatusInternacao.class, status.getType());

    Field historicoId = HistoricoStatusLeito.class.getDeclaredField("id");
    assertEquals(GenerationType.UUID, historicoId.getAnnotation(GeneratedValue.class).strategy());

    Field autor = HistoricoStatusLeito.class.getDeclaredField("autor");
    assertEquals(EnumType.STRING, autor.getAnnotation(Enumerated.class).value());
    assertEquals(Autor.class, autor.getType());

    Field evento = HistoricoStatusLeito.class.getDeclaredField("evento");
    assertEquals(EventoLeito.class, evento.getType());

    Field statusNovo = HistoricoStatusLeito.class.getDeclaredField("statusNovo");
    assertEquals(StatusLeito.class, statusNovo.getType());
  }

  @Test
  void leitoMantemVersion() throws Exception {
    assertNotNull(Leito.class.getDeclaredField("versao").getAnnotation(Version.class));
    assertTrue(Arrays.stream(Leito.class.getMethods())
        .map(Method::getName)
        .anyMatch(name -> name.equals("aplicarTransicao")));
  }
}
