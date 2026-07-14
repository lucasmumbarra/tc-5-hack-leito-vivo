package br.com.leitovivo.service;

import br.com.leitovivo.domain.StatusLeito;
import br.com.leitovivo.domain.sla.AcaoAutomaticaSla;
import br.com.leitovivo.exception.PayloadInvalidoException;
import br.com.leitovivo.exception.RecursoNaoEncontradoException;
import br.com.leitovivo.persistence.SlaStatusLeito;
import br.com.leitovivo.persistence.SlaStatusLeitoRepository;
import br.com.leitovivo.web.dto.AtualizarSlaRequest;
import br.com.leitovivo.web.dto.SlaResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlaServiceTest {

    @Mock
    private SlaStatusLeitoRepository slaStatusLeitoRepository;

    private SlaService slaService;
    private SlaStatusLeito sla;
    private UUID slaId;

    @BeforeEach
    void setUp() {
        slaService = new SlaService(slaStatusLeitoRepository);
        slaId = UUID.randomUUID();
        sla = new SlaStatusLeito(slaId, null, null, StatusLeito.OCUPADO, 28800, null, AcaoAutomaticaSla.NENHUMA);
    }

    @Test
    void listarRetornaTodos() {
        when(slaStatusLeitoRepository.findAll()).thenReturn(List.of(sla));
        assertEquals(1, slaService.listar().size());
    }

    @Test
    void atualizarAlteraPrazo() {
        when(slaStatusLeitoRepository.findById(slaId)).thenReturn(Optional.of(sla));

        SlaResponse response = slaService.atualizar(
                slaId, new AtualizarSlaRequest(2, null, AcaoAutomaticaSla.NENHUMA));

        assertEquals(2, response.prazoAlertaMin());
    }

    @Test
    void atualizarIdInexistente404() {
        when(slaStatusLeitoRepository.findById(slaId)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () ->
                slaService.atualizar(slaId, new AtualizarSlaRequest(10, null, AcaoAutomaticaSla.NENHUMA)));
    }

    @Test
    void atualizarPrazoInvalido422() {
        assertThrows(PayloadInvalidoException.class, () ->
                slaService.atualizar(slaId, new AtualizarSlaRequest(0, null, AcaoAutomaticaSla.NENHUMA)));
    }
}
