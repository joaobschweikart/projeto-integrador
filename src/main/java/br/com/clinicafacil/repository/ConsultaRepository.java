package br.com.clinicafacil.repository;

import br.com.clinicafacil.entity.Consulta;
import br.com.clinicafacil.entity.enums.StatusConsultaEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositório JPA para a entidade Consulta.
 *
 * A query de verificação de conflito implementa a regra de negócio
 * central do ClinicaFácil: "agendamento com verificação automática
 * de conflitos".
 *
 * Conforme o Diagrama de Sequência :
 *   ConsultaService → verificarConflito() → ConsultaRepository
 *   A verificação ocorre ANTES do save(), garantindo integridade.
 *
 * Casos de uso: UC02 (Agendar), UC03 (Cancelar), UC04 (Remarcar),
 *               UC05 (Visualizar Agenda), UC10 (Gerar Relatório).
 */
@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    /**
     * Verifica se existe conflito de horário para um médico.
     *
     * Regra de negócio: um médico não pode ter duas consultas no mesmo
     * horário com status AGENDADA ou CONFIRMADA.
     * Conforme a seção 1.3 (Proposta de Valor): "eliminação de conflitos
     * de horário por meio de agenda digital em tempo real".
     *
     * @param medicoId   ID do médico
     * @param dataHora   Data e hora desejada
     * @param statusList Status que representam conflito ativo
     * @return true se houver conflito
     */
    @Query("""
            SELECT COUNT(c) > 0 FROM Consulta c
            WHERE c.medico.id = :medicoId
              AND c.dataHora = :dataHora
              AND c.status IN :statusList
            """)
    boolean existeConflito(
            @Param("medicoId") Long medicoId,
            @Param("dataHora") LocalDateTime dataHora,
            @Param("statusList") List<StatusConsultaEnum> statusList
    );

    /**
     * Variação para remarcar: exclui a própria consulta da verificação.
     */
    @Query("""
            SELECT COUNT(c) > 0 FROM Consulta c
            WHERE c.medico.id = :medicoId
              AND c.dataHora = :dataHora
              AND c.status IN :statusList
              AND c.id <> :consultaId
            """)
    boolean existeConflitoExcluindoConsulta(
            @Param("medicoId") Long medicoId,
            @Param("dataHora") LocalDateTime dataHora,
            @Param("statusList") List<StatusConsultaEnum> statusList,
            @Param("consultaId") Long consultaId
    );

    /** Lista consultas de um médico em um intervalo de datas (agenda diária/semanal/mensal). */
    @Query("""
            SELECT c FROM Consulta c
            WHERE c.medico.id = :medicoId
              AND c.dataHora BETWEEN :inicio AND :fim
            ORDER BY c.dataHora
            """)
    List<Consulta> findByMedicoIdAndPeriodo(
            @Param("medicoId") Long medicoId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    /** Lista consultas de um paciente (histórico). */
    List<Consulta> findByPacienteIdOrderByDataHoraDesc(Long pacienteId);

    /** Lista consultas por status (ex.: todas as CANCELADAS para relatório). */
    List<Consulta> findByStatus(StatusConsultaEnum status);

    /**
     * Conta consultas por status — base para relatório gerencial.
     * Conforme UC10 - Gerar Relatório e o painel administrativo.
     */
    long countByStatus(StatusConsultaEnum status);
}
