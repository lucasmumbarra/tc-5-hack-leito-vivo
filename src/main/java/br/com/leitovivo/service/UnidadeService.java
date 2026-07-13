package br.com.leitovivo.service;

import br.com.leitovivo.exception.PayloadInvalidoException;
import br.com.leitovivo.exception.RecursoNaoEncontradoException;
import br.com.leitovivo.persistence.Unidade;
import br.com.leitovivo.persistence.UnidadeRepository;
import br.com.leitovivo.web.dto.CriarUnidadeRequest;
import br.com.leitovivo.web.dto.UnidadeResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UnidadeService {

    private final UnidadeRepository unidadeRepository;

    public UnidadeService(UnidadeRepository unidadeRepository) {
        this.unidadeRepository = unidadeRepository;
    }

    @Transactional
    public UnidadeResponse criar(CriarUnidadeRequest request) {
        validar(request);
        Unidade salva = unidadeRepository.save(
                new Unidade(request.nome().trim(), request.municipio().trim(), request.regiao().trim(), request.tipo().trim()));
        return toResponse(salva);
    }

    @Transactional(readOnly = true)
    public UnidadeResponse buscarPorId(UUID id) {
        return unidadeRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Unidade não encontrada: " + id));
    }

    private void validar(CriarUnidadeRequest request) {
        if (request == null) {
            throw new PayloadInvalidoException("Payload obrigatório");
        }
        requireText(request.nome(), "nome");
        requireText(request.municipio(), "municipio");
        requireText(request.regiao(), "regiao");
        requireText(request.tipo(), "tipo");
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new PayloadInvalidoException(field + " é obrigatório");
        }
    }

    private UnidadeResponse toResponse(Unidade unidade) {
        return new UnidadeResponse(
                unidade.getId(),
                unidade.getNome(),
                unidade.getMunicipio(),
                unidade.getRegiao(),
                unidade.getTipo());
    }
}
