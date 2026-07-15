package br.com.leitovivo.domain.sla;

import br.com.leitovivo.domain.leito.enums.StatusLeito;
import br.com.leitovivo.domain.sla.enums.AcaoAutomatica;
import br.com.leitovivo.domain.sla.enums.DecisaoSla;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RegraAlertaSlaTest {

    private static final Instant AGORA = Instant.parse("2026-07-13T18:00:00Z");

    @Test
    void leitoFantasmaGeraAlerta() {
        Instant ultima = AGORA.minus(25, ChronoUnit.DAYS);
        DecisaoSla d = RegraAlertaSla.avaliar(
                StatusLeito.OCUPADO, ultima, 20 * 24 * 60, null, AcaoAutomatica.NENHUMA, AGORA);
        assertEquals(DecisaoSla.ABRIR_ALERTA, d);
    }

    @Test
    void higienizacaoEsquecidaGeraAlerta() {
        Instant ultima = AGORA.minus(3, ChronoUnit.HOURS);
        DecisaoSla d = RegraAlertaSla.avaliar(
                StatusLeito.EM_HIGIENIZACAO, ultima, 120, 240, AcaoAutomatica.LIBERAR_LEITO, AGORA);
        assertEquals(DecisaoSla.ABRIR_ALERTA, d);
    }

    @Test
    void higienizacaoAlemDoPrazoAcaoLibera() {
        Instant ultima = AGORA.minus(5, ChronoUnit.HOURS);
        DecisaoSla d = RegraAlertaSla.avaliar(
                StatusLeito.EM_HIGIENIZACAO, ultima, 120, 240, AcaoAutomatica.LIBERAR_LEITO, AGORA);
        assertEquals(DecisaoSla.ABRIR_ALERTA_E_LIBERAR, d);
    }

    @Test
    void reservaZumbiGeraAlerta() {
        Instant ultima = AGORA.minus(7, ChronoUnit.HOURS);
        DecisaoSla d = RegraAlertaSla.avaliar(
                StatusLeito.RESERVADO, ultima, 360, null, AcaoAutomatica.NENHUMA, AGORA);
        assertEquals(DecisaoSla.ABRIR_ALERTA, d);
    }

    @Test
    void leitoDentroDoPrazoNada() {
        Instant ultima = AGORA.minus(30, ChronoUnit.MINUTES);
        DecisaoSla d = RegraAlertaSla.avaliar(
                StatusLeito.EM_HIGIENIZACAO, ultima, 120, 240, AcaoAutomatica.LIBERAR_LEITO, AGORA);
        assertEquals(DecisaoSla.NADA, d);
    }

    @Test
    void limiteExatoGeraAlerta() {
        Instant ultima = AGORA.minus(120, ChronoUnit.MINUTES);
        DecisaoSla d = RegraAlertaSla.avaliar(
                StatusLeito.EM_HIGIENIZACAO, ultima, 120, 240, AcaoAutomatica.LIBERAR_LEITO, AGORA);
        assertEquals(DecisaoSla.ABRIR_ALERTA, d);
    }

    @Test
    void acaoNenhumaGeraAlertaSemLiberar() {
        Instant ultima = AGORA.minus(25, ChronoUnit.DAYS);
        DecisaoSla d = RegraAlertaSla.avaliar(
                StatusLeito.OCUPADO, ultima, 20 * 24 * 60, null, AcaoAutomatica.NENHUMA, AGORA);
        assertEquals(DecisaoSla.ABRIR_ALERTA, d);
    }
}
