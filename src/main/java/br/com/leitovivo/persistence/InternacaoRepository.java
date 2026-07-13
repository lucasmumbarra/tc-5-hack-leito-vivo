package br.com.leitovivo.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InternacaoRepository extends JpaRepository<Internacao, UUID> {

    boolean existsByPacienteIdAndStatus(UUID pacienteId, StatusInternacao status);

    long countByLeitoIdAndStatus(UUID leitoId, StatusInternacao status);

    Optional<Internacao> findByLeitoIdAndStatus(UUID leitoId, StatusInternacao status);
}
