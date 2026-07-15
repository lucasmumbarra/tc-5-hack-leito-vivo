package br.com.leitovivo.persistence.repository;

import br.com.leitovivo.persistence.entity.Paciente;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PacienteRepository extends JpaRepository<Paciente, UUID> {
}
