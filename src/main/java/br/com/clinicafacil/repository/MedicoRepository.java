package br.com.clinicafacil.repository;

import br.com.clinicafacil.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório JPA para a entidade Medico.
 *
 * Caso de uso: UC08 - Cadastrar Medico (Administrador).
 *
 * A query customizada busca médicos com horário disponível em
 * uma especialidade específica — consumida pelo Diagrama de Sequência
 *  na etapa "GET /medicos?especialidade=X&data=Y".
 */
@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {

    /** Busca médico pelo CRM único. */
    Optional<Medico> findByCrm(String crm);

    /** Verifica se já existe médico com o CRM informado. */
    boolean existsByCrm(String crm);

    /**
     * Lista todos os médicos de uma especialidade.
     * Útil para a seleção de especialidade na tela de agendamento.
     */
    List<Medico> findByEspecialidadeId(Long especialidadeId);

    /**
     * Busca médicos por nome (busca parcial, case-insensitive).
     */
    List<Medico> findByNomeContainingIgnoreCase(String nome);

    /**
     * Lista médicos que possuem pelo menos um horário disponível
     * em uma determinada especialidade.
     * Conforme o Diagrama de Sequência :
     *   "GET /medicos?especialidade=X&data=Y → findDisponivel(esp, data)"
     */
    @Query("""
            SELECT DISTINCT m FROM Medico m
            JOIN m.horariosAtendimento h
            WHERE m.especialidade.id = :especialidadeId
            ORDER BY m.nome
            """)
    List<Medico> findDisponiveisPorEspecialidade(@Param("especialidadeId") Long especialidadeId);
}
