package br.com.clinicafacil.dto.request;

import br.com.clinicafacil.entity.enums.DiaSemanaEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTOs de requisição (entrada da API).
 *
 * Separar DTO de entidade é boa prática: protege campos internos,
 * permite validações independentes e desacopla o contrato da API
 * do modelo de persistência.
 * Conforme boas práticas orientadas na disciplina.
 */
public class RequestDTOs {

    // ─────────────────────────────────────────────
    // ESPECIALIDADE
    // ─────────────────────────────────────────────

    @Schema(description = "Dados para cadastro ou atualização de uma especialidade médica")
    public record EspecialidadeRequest(

            @Schema(description = "Nome da especialidade", example = "Cardiologia")
            @NotBlank(message = "Nome é obrigatório")
            @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
            String nome,

            @Schema(description = "Descrição opcional da especialidade", example = "Tratamento de doenças do coração")
            @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
            String descricao
    ) {}

    // ─────────────────────────────────────────────
    // PACIENTE
    // ─────────────────────────────────────────────

    @Schema(description = "Dados para cadastro ou atualização de um paciente")
    public record PacienteRequest(

            @Schema(description = "Nome completo", example = "Maria da Silva")
            @NotBlank(message = "Nome é obrigatório")
            @Size(max = 150)
            String nome,

            @Schema(description = "E-mail para login e notificações", example = "maria@email.com")
            @NotBlank(message = "E-mail é obrigatório")
            @Email(message = "E-mail inválido")
            String email,

            @Schema(description = "Senha de acesso", example = "Senha@123")
            @NotBlank(message = "Senha é obrigatória")
            @Size(min = 6, message = "Senha deve ter ao menos 6 caracteres")
            String senha,

            @Schema(description = "CPF no formato 000.000.000-00", example = "123.456.789-00")
            @NotBlank(message = "CPF é obrigatório")
            String cpf,

            @Schema(description = "Data de nascimento", example = "1990-05-20")
            LocalDate dataNascimento,

            @Schema(description = "Telefone com DDD", example = "(49) 99999-0000")
            @Size(max = 20)
            String telefone
    ) {}

    // ─────────────────────────────────────────────
    // MÉDICO
    // ─────────────────────────────────────────────

    @Schema(description = "Dados para cadastro ou atualização de um médico")
    public record MedicoRequest(

            @Schema(description = "Nome completo", example = "Dr. Carlos Souza")
            @NotBlank(message = "Nome é obrigatório")
            @Size(max = 150)
            String nome,

            @Schema(description = "E-mail profissional", example = "carlos@clinica.com")
            @NotBlank(message = "E-mail é obrigatório")
            @Email(message = "E-mail inválido")
            String email,

            @Schema(description = "Senha de acesso", example = "Senha@123")
            @NotBlank(message = "Senha é obrigatória")
            @Size(min = 6)
            String senha,

            @Schema(description = "CRM do médico", example = "SC-12345")
            @NotBlank(message = "CRM é obrigatório")
            @Size(max = 20)
            String crm,

            @Schema(description = "ID da especialidade do médico", example = "1")
            @NotNull(message = "Especialidade é obrigatória")
            Long especialidadeId
    ) {}

    // ─────────────────────────────────────────────
    // HORÁRIO DISPONÍVEL
    // ─────────────────────────────────────────────

    @Schema(description = "Dados para cadastro de horário disponível de um médico")
    public record HorarioDisponivelRequest(

            @Schema(description = "ID do médico", example = "1")
            @NotNull(message = "ID do médico é obrigatório")
            Long medicoId,

            @Schema(description = "Dia da semana", example = "SEGUNDA")
            @NotNull(message = "Dia da semana é obrigatório")
            DiaSemanaEnum diaSemana,

            @Schema(description = "Hora de início", example = "08:00")
            @NotNull(message = "Hora de início é obrigatória")
            LocalTime horaInicio,

            @Schema(description = "Hora de fim", example = "12:00")
            @NotNull(message = "Hora de fim é obrigatória")
            LocalTime horaFim
    ) {}
}
