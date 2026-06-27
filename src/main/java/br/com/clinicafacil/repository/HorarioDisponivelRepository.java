package br.com.clinicafacil.repository;

import br.com.clinicafacil.entity.HorarioDisponivel;
import br.com.clinicafacil.entity.enums.DiaSemanaEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório JPA para HorarioDisponivel.
 *
 * Conforme o Diagrama de Sequência :
 *   A busca de horários disponíveis é o segundo passo do fluxo
 *   de agendamento, após a seleção da especialidade.
 *   "SELECT horarios livres → horarios"
 */
@Repository
public interface HorarioDisponivelRepository extends JpaRepository<HorarioDisponivel, Long> {

    /** Lista todos os horários de um médico específico. */
    List<HorarioDisponivel> findByMedicoId(Long medicoId);

    /** Lista horários de um médico em um dia da semana específico. */
    List<HorarioDisponivel> findByMedicoIdAndDiaSemana(Long medicoId, DiaSemanaEnum diaSemana);
}
