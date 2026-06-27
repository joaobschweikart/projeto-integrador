package br.com.clinicafacil.dto.response;

import br.com.clinicafacil.entity.*;
import br.com.clinicafacil.entity.enums.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTOs de resposta da API.
 *
 * Usados para controlar exatamente o que a API expõe ao cliente,
 * sem vazar campos sensíveis (ex.: senha_hash) e sem causar
 * problemas de serialização circular (ex.: Consulta → Medico → Consulta...).
 *
 * Seguem as boas práticas de código limpo e separação de camadas
 * orientadas na disciplina.
 */
public class ResponseDTOs {

    // ─────────────────────────────────────────────
    // ESPECIALIDADE
    // ─────────────────────────────────────────────

    @Schema(description = "Dados de retorno de uma especialidade")
    public record EspecialidadeResponse(
            Long id,
            String nome,
            String descricao
    ) {
        public static EspecialidadeResponse from(Especialidade e) {
            return new EspecialidadeResponse(e.getId(), e.getNome(), e.getDescricao());
        }
    }

    // ─────────────────────────────────────────────
    // PACIENTE
    // ─────────────────────────────────────────────

    @Schema(description = "Dados de retorno de um paciente (sem senha)")
    public record PacienteResponse(
            Long id,
            String nome,
            String email,
            String cpf,
            LocalDate dataNascimento,
            String telefone,
            String historico,
            PerfilEnum perfil
    ) {
        public static PacienteResponse from(Paciente p) {
            return new PacienteResponse(
                    p.getId(), p.getNome(), p.getEmail(),
                    p.getCpf(), p.getDataNascimento(), p.getTelefone(),
                    p.getHistorico(), p.getPerfil()
            );
        }
    }

    // ─────────────────────────────────────────────
    // MÉDICO
    // ─────────────────────────────────────────────

    @Schema(description = "Dados de retorno de um médico (sem senha)")
    public record MedicoResponse(
            Long id,
            String nome,
            String email,
            String crm,
            EspecialidadeResponse especialidade,
            PerfilEnum perfil
    ) {
        public static MedicoResponse from(Medico m) {
            return new MedicoResponse(
                    m.getId(), m.getNome(), m.getEmail(), m.getCrm(),
                    m.getEspecialidade() != null ? EspecialidadeResponse.from(m.getEspecialidade()) : null,
                    m.getPerfil()
            );
        }
    }

    // ─────────────────────────────────────────────
    // HORÁRIO DISPONÍVEL
    // ─────────────────────────────────────────────

    @Schema(description = "Horário de atendimento disponível de um médico")
    public record HorarioDisponivelResponse(
            Long id,
            Long medicoId,
            String medicoNome,
            DiaSemanaEnum diaSemana,
            LocalTime horaInicio,
            LocalTime horaFim
    ) {
        public static HorarioDisponivelResponse from(HorarioDisponivel h) {
            return new HorarioDisponivelResponse(
                    h.getId(),
                    h.getMedico().getId(),
                    h.getMedico().getNome(),
                    h.getDiaSemana(),
                    h.getHoraInicio(),
                    h.getHoraFim()
            );
        }
    }

    // ─────────────────────────────────────────────
    // CONSULTA
    // ─────────────────────────────────────────────

    @Schema(description = "Dados de retorno de uma consulta")
    public record ConsultaResponse(
            Long id,
            Long pacienteId,
            String pacienteNome,
            Long medicoId,
            String medicoNome,
            String medicoCrm,
            LocalDateTime dataHora,
            StatusConsultaEnum status,
            String observacao,
            LocalDateTime criadoEm
    ) {
        public static ConsultaResponse from(Consulta c) {
            return new ConsultaResponse(
                    c.getId(),
                    c.getPaciente().getId(),
                    c.getPaciente().getNome(),
                    c.getMedico().getId(),
                    c.getMedico().getNome(),
                    c.getMedico().getCrm(),
                    c.getDataHora(),
                    c.getStatus(),
                    c.getObservacao(),
                    c.getCriadoEm()
            );
        }
    }

    // ─────────────────────────────────────────────
    // RELATÓRIO GERENCIAL (UC10)
    // ─────────────────────────────────────────────

    @Schema(description = "Relatório de ocupação e comparecimento")
    public record RelatorioResponse(
            long totalConsultas,
            long agendadas,
            long confirmadas,
            long realizadas,
            long canceladas,
            long naoCompareceu,
            double taxaComparecimento
    ) {}

    // ─────────────────────────────────────────────
    // NOTIFICAÇÃO
    // ─────────────────────────────────────────────

    @Schema(description = "Dados de retorno de uma notificação")
    public record NotificacaoResponse(
            Long id,
            Long consultaId,
            TipoNotificacaoEnum tipo,
            String mensagem,
            LocalDateTime enviadoEm
    ) {
        public static NotificacaoResponse from(Notificacao n) {
            return new NotificacaoResponse(
                    n.getId(),
                    n.getConsulta().getId(),
                    n.getTipo(),
                    n.getMensagem(),
                    n.getEnviadoEm()
            );
        }
    }
}
