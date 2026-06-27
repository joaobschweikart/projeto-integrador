package br.com.clinicafacil.entity.enums;

/**
 * Perfis de acesso do sistema ClinicaFácil.
 *
 * Conforme modelado no Diagrama de Classes :
 * - ADMINISTRADOR: acesso total, gerencia médicos, especialidades e relatórios.
 * - RECEPCIONISTA:  agenda, cancela e remarca consultas; cadastra pacientes.
 * - MEDICO:         visualiza agenda e registra observações.
 * - PACIENTE:       agenda consultas e recebe notificações.
 *
 * Usado na estratégia SINGLE_TABLE da entidade Usuario para
 * distinguir o tipo de usuário sem múltiplas tabelas.
 */
public enum PerfilEnum {
    ADMINISTRADOR,
    RECEPCIONISTA,
    MEDICO,
    PACIENTE
}
