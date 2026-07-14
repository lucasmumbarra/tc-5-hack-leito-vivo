package br.com.leitovivo.persistence.repository;

import br.com.leitovivo.persistence.entity.Internacao;
import br.com.leitovivo.persistence.enums.StatusInternacao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InternacaoRepository extends JpaRepository<Internacao, UUID> {

    boolean existsByPacienteIdAndStatus(UUID pacienteId, StatusInternacao status);

    long countByLeitoIdAndStatus(UUID leitoId, StatusInternacao status);

    Optional<Internacao> findByLeitoIdAndStatus(UUID leitoId, StatusInternacao status);
}
