package br.com.leitovivo.service;

import br.com.leitovivo.domain.leito.enums.Autor;
import br.com.leitovivo.domain.leito.enums.EventoLeito;
import br.com.leitovivo.domain.leito.enums.StatusLeito;
import br.com.leitovivo.exception.ConflitoNegocioException;
import br.com.leitovivo.exception.PayloadInvalidoException;
import br.com.leitovivo.exception.RecursoNaoEncontradoException;
import br.com.leitovivo.persistence.entity.Internacao;
import br.com.leitovivo.persistence.repository.InternacaoRepository;
import br.com.leitovivo.persistence.entity.Leito;
import br.com.leitovivo.persistence.repository.LeitoRepository;
import br.com.leitovivo.persistence.entity.Paciente;
import br.com.leitovivo.persistence.repository.PacienteRepository;
import br.com.leitovivo.persistence.enums.StatusInternacao;
import br.com.leitovivo.web.dto.request.CriarInternacaoRequest;
import br.com.leitovivo.web.dto.response.InternacaoResponse;
import br.com.leitovivo.web.mapper.InternacaoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@Service
public class InternacaoService {

    private static final Set<StatusLeito> STATUS_PERMITIDOS_INTERNACAO =
            EnumSet.of(StatusLeito.LIVRE, StatusLeito.RESERVADO);

    private final InternacaoRepository internacaoRepository;
    private final LeitoRepository leitoRepository;
    private final PacienteRepository pacienteRepository;
    private final LeitoService leitoService;
    private final Clock clock;

    public InternacaoService(
            InternacaoRepository internacaoRepository,
            LeitoRepository leitoRepository,
            PacienteRepository pacienteRepository,
            LeitoService leitoService,
            Clock clock) {
        this.internacaoRepository = internacaoRepository;
        this.leitoRepository = leitoRepository;
        this.pacienteRepository = pacienteRepository;
        this.leitoService = leitoService;
        this.clock = clock;
    }

    @Transactional
    public InternacaoResponse internar(CriarInternacaoRequest request) {
        validar(request);

        Paciente paciente = pacienteRepository.findById(request.pacienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Paciente não encontrado: " + request.pacienteId()));

        if (internacaoRepository.existsByPacienteIdAndStatus(paciente.getId(), StatusInternacao.ATIVA)) {
            throw new ConflitoNegocioException("Paciente já possui internação ATIVA");
        }

        Leito leito = leitoRepository.findById(request.leitoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Leito não encontrado: " + request.leitoId()));

        if (!STATUS_PERMITIDOS_INTERNACAO.contains(leito.getStatus())) {
            throw new ConflitoNegocioException(
                    "Internação bloqueada: leito em status " + leito.getStatus());
        }

        // Funil primeiro: garante @Version e histórico; se falhar, nada de internação fica gravado.
        leitoService.transicionar(
                leito.getId(),
                EventoLeito.INTERNAR_PACIENTE,
                Autor.USUARIO,
                request.motivo());

        Instant agora = Instant.now(clock);
        Internacao internacao = internacaoRepository.save(new Internacao(leito, paciente, agora));
        return InternacaoMapper.toResponse(internacao);
    }

    @Transactional
    public InternacaoResponse registrarAlta(UUID internacaoId, String motivo) {
        Internacao internacao = internacaoRepository.findById(internacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Internação não encontrada: " + internacaoId));

        if (internacao.getStatus() == StatusInternacao.ENCERRADA) {
            throw new ConflitoNegocioException("Internação já encerrada");
        }

        Instant agora = Instant.now(clock);
        internacao.encerrar(agora);
        leitoService.transicionar(
                internacao.getLeito().getId(),
                EventoLeito.REGISTRAR_ALTA,
                Autor.USUARIO,
                motivo);

        return InternacaoMapper.toResponse(internacao);
    }

    private void validar(CriarInternacaoRequest request) {
        if (request == null) {
            throw new PayloadInvalidoException("Payload obrigatório");
        }
        if (request.leitoId() == null) {
            throw new PayloadInvalidoException("leitoId é obrigatório");
        }
        if (request.pacienteId() == null) {
            throw new PayloadInvalidoException("pacienteId é obrigatório");
        }
    }

}
