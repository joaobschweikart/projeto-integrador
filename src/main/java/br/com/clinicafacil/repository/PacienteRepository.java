package br.com.clinicafacil.repository;

import br.com.clinicafacil.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório JPA para a entidade Paciente.
 *
 * Herda de JpaRepository todos os métodos CRUD básicos.
 * Métodos customizados usam a convenção de nomes do Spring Data
 * (query derivation) — nenhuma @Query SQL manual é necessária.
 *
 * Caso de uso: UC07 - Cadastrar Paciente (Recepcionista).
 */
@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    /** Busca paciente pelo CPF. */
    Optional<Paciente> findByCpf(String cpf);

    /** Verifica se já existe paciente com o CPF informado. */
    boolean existsByCpf(String cpf);

    /** Verifica se já existe paciente com o e-mail informado. */
    boolean existsByEmail(String email);

    /**
     * Busca pacientes cujo nome contenha o termo informado (case-insensitive).
     * Útil para o campo de busca de pacientes da recepcionista.
     */
    List<Paciente> findByNomeContainingIgnoreCase(String nome);
}
