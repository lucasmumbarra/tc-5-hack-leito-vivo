package br.com.leitovivo.persistence.repository;

import br.com.leitovivo.persistence.entity.Paciente;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PacienteRepository extends JpaRepository<Paciente, UUID> {
}
