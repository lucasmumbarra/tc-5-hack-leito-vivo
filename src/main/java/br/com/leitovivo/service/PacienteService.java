package br.com.leitovivo.service;

import br.com.leitovivo.exception.PayloadInvalidoException;
import br.com.leitovivo.persistence.entity.Paciente;
import br.com.leitovivo.persistence.repository.PacienteRepository;
import br.com.leitovivo.web.dto.request.CriarPacienteRequest;
import br.com.leitovivo.web.dto.response.PacienteResponse;
import br.com.leitovivo.web.mapper.PacienteMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    public PacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    @Transactional
    public PacienteResponse criar(CriarPacienteRequest request) {
        validar(request);
        Paciente salvo = pacienteRepository.save(
                new Paciente(request.nome().trim(), request.dataNascimento(), request.cartaoSus().trim()));
        return PacienteMapper.toResponse(salvo);
    }

    private void validar(CriarPacienteRequest request) {
        if (request == null) {
            throw new PayloadInvalidoException("Payload obrigatório");
        }
        if (request.nome() == null || request.nome().isBlank()) {
            throw new PayloadInvalidoException("nome é obrigatório");
        }
        if (request.dataNascimento() == null) {
            throw new PayloadInvalidoException("dataNascimento é obrigatório");
        }
        if (request.cartaoSus() == null || request.cartaoSus().isBlank()) {
            throw new PayloadInvalidoException("cartaoSus é obrigatório");
        }
    }

}
