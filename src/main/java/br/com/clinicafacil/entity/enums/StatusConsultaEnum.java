package br.com.clinicafacil.entity.enums;

/**
 * Status possíveis de uma Consulta no ClinicaFácil.
 *
 * Conforme o atributo "status: StatusEnum" do Diagrama de Classes 
 * e os casos de uso UC02 (Agendar), UC03 (Cancelar) e UC04 (Remarcar).
 */
public enum StatusConsultaEnum {
    /** Consulta marcada e aguardando confirmação ou atendimento. */
    AGENDADA,

    /** Consulta confirmada pelo paciente ou recepcionista. */
    CONFIRMADA,

    /** Consulta cancelada — libera o horário na agenda do médico. */
    CANCELADA,

    /** Paciente não compareceu (no-show). */
    NAO_COMPARECEU,

    /** Atendimento realizado com sucesso. */
    REALIZADA
}
