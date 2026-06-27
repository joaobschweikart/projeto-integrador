package br.com.clinicafacil.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * DTOs relacionados a Consulta.
 *
 * Separado em arquivo próprio pela maior complexidade das
 * operações de agendamento (UC02, UC03, UC04 do Diagrama de Casos de Uso).
 */
public class ConsultaRequestDTOs {

    /**
     * Request para agendar uma nova consulta.
     * Conforme o fluxo do Diagrama de Sequência :
     *   POST /consultas → agendarConsulta(dto) → verificarConflito() → save()
     */
    @Schema(description = "Dados para agendamento de uma nova consulta")
    public record AgendarConsultaRequest(

            @Schema(description = "ID do paciente", example = "1")
            @NotNull(message = "ID do paciente é obrigatório")
            Long pacienteId,

            @Schema(description = "ID do médico", example = "2")
            @NotNull(message = "ID do médico é obrigatório")
            Long medicoId,

            @Schema(description = "Data e hora desejada para a consulta", example = "2026-06-01T09:00:00")
            @NotNull(message = "Data e hora são obrigatórias")
            @Future(message = "A consulta deve ser agendada para uma data futura")
            LocalDateTime dataHora
    ) {}

    /**
     * Request para remarcar uma consulta existente.
     * Conforme UC04 - Remarcar Consulta e o método +remarcar() da entidade.
     */
    @Schema(description = "Nova data/hora para remarcação de consulta")
    public record RemarcarConsultaRequest(

            @Schema(description = "Nova data e hora da consulta", example = "2026-06-10T14:00:00")
            @NotNull(message = "Nova data e hora são obrigatórias")
            @Future(message = "A nova data deve ser futura")
            LocalDateTime novaDataHora
    ) {}

    /**
     * Request para registrar observação do médico.
     * Conforme UC11 - Registrar Observação (Médico).
     */
    @Schema(description = "Observação do médico sobre a consulta realizada")
    public record ObservacaoRequest(

            @Schema(description = "Texto da observação clínica", example = "Paciente com pressão elevada. Retornar em 30 dias.")
            @NotNull(message = "Observação é obrigatória")
            String observacao
    ) {}
}
