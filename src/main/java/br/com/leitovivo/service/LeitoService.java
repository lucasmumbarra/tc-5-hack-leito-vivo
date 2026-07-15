package br.com.leitovivo.service;

import br.com.leitovivo.domain.leito.MaquinaEstadosLeito;
import br.com.leitovivo.domain.leito.enums.Autor;
import br.com.leitovivo.domain.leito.enums.EventoLeito;
import br.com.leitovivo.domain.leito.enums.StatusLeito;
import br.com.leitovivo.domain.leito.enums.TipoLeito;
import br.com.leitovivo.exception.ConflitoNegocioException;
import br.com.leitovivo.exception.PayloadInvalidoException;
import br.com.leitovivo.exception.RecursoNaoEncontradoException;
import br.com.leitovivo.persistence.entity.HistoricoStatusLeito;
import br.com.leitovivo.persistence.entity.Leito;
import br.com.leitovivo.persistence.entity.Unidade;
import br.com.leitovivo.persistence.repository.HistoricoStatusLeitoRepository;
import br.com.leitovivo.persistence.repository.LeitoRepository;
import br.com.leitovivo.persistence.repository.UnidadeRepository;
import br.com.leitovivo.web.dto.request.CriarLeitoRequest;
import br.com.leitovivo.web.dto.request.TransicionarLeitoRequest;
import br.com.leitovivo.web.dto.response.HistoricoStatusResponse;
import br.com.leitovivo.web.dto.response.LeitoResponse;
import br.com.leitovivo.web.mapper.LeitoMapper;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeitoService {

  private final LeitoRepository leitoRepository;
  private final UnidadeRepository unidadeRepository;
  private final HistoricoStatusLeitoRepository historicoStatusLeitoRepository;
  private final Clock clock;

  public LeitoService(
      LeitoRepository leitoRepository,
      UnidadeRepository unidadeRepository,
      HistoricoStatusLeitoRepository historicoStatusLeitoRepository,
      Clock clock) {
    this.leitoRepository = leitoRepository;
    this.unidadeRepository = unidadeRepository;
    this.historicoStatusLeitoRepository = historicoStatusLeitoRepository;
    this.clock = clock;
  }

  @Transactional
  public LeitoResponse criar(CriarLeitoRequest request) {
    validar(request);
    Unidade unidade = unidadeRepository.findById(request.unidadeId())
        .orElseThrow(() -> new RecursoNaoEncontradoException(
            "Unidade não encontrada: " + request.unidadeId()));

    String codigo = request.codigo().trim();
    if (leitoRepository.existsByUnidadeIdAndCodigo(unidade.getId(), codigo)) {
      throw new ConflitoNegocioException(
          "Já existe leito com código '" + codigo + "' na unidade " + unidade.getId());
    }

    Instant agora = Instant.now(clock);
    Leito leito = new Leito(
        unidade,
        codigo,
        request.tipo(),
        StatusLeito.LIVRE,
        false,
        agora);
    return LeitoMapper.toResponse(leitoRepository.save(leito));
  }

  @Transactional(readOnly = true)
  public List<LeitoResponse> listar(UUID unidadeId, TipoLeito tipo, StatusLeito status) {
    return leitoRepository.filtrar(unidadeId, tipo, status).stream()
        .map(LeitoMapper::toResponse)
        .toList();
  }

  /**
   * Funil único de transição: único ponto que altera status após a criação.
   */
  @Transactional
  public LeitoResponse transicionar(UUID leitoId, EventoLeito evento, Autor autor, String motivo) {
    if (evento == null) {
      throw new PayloadInvalidoException("evento é obrigatório");
    }
    if (autor == null) {
      throw new PayloadInvalidoException("autor é obrigatório");
    }
    Leito leito = leitoRepository.findById(leitoId)
        .orElseThrow(() -> new RecursoNaoEncontradoException("Leito não encontrado: " + leitoId));

    StatusLeito anterior = leito.getStatus();
    StatusLeito novo = MaquinaEstadosLeito.transicionar(anterior, evento);
    Instant agora = Instant.now(clock);

    leito.aplicarTransicao(novo, agora);
    historicoStatusLeitoRepository.save(new HistoricoStatusLeito(
        leito, anterior, novo, evento, autor, motivo, agora));

    return LeitoMapper.toResponse(leito);
  }

  @Transactional
  public LeitoResponse transicionar(UUID leitoId, TransicionarLeitoRequest request) {
    return transicionar(leitoId, request.evento(), request.autor(), request.motivo());
  }

  @Transactional(readOnly = true)
  public List<HistoricoStatusResponse> listarHistorico(UUID leitoId) {
    if (!leitoRepository.existsById(leitoId)) {
      throw new RecursoNaoEncontradoException("Leito não encontrado: " + leitoId);
    }
    return historicoStatusLeitoRepository.findByLeitoIdOrderByDataHoraAsc(leitoId).stream()
        .map(LeitoMapper::toHistoricoResponse)
        .toList();
  }

  private void validar(CriarLeitoRequest request) {
    if (request == null) {
      throw new PayloadInvalidoException("Payload obrigatório");
    }
    if (request.unidadeId() == null) {
      throw new PayloadInvalidoException("unidadeId é obrigatório");
    }
    if (request.codigo() == null || request.codigo().isBlank()) {
      throw new PayloadInvalidoException("codigo é obrigatório");
    }
    if (request.tipo() == null) {
      throw new PayloadInvalidoException("tipo é obrigatório");
    }
  }

}
