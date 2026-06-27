package br.com.clinicafacil.entity.enums;

/**
 * Tipos de notificação enviadas no ClinicaFácil.
 *
 * Conforme o atributo "tipo: TipoNotificacaoEnum" do Diagrama
 * de Classes  e a funcionalidade de envio de lembretes
 * por e-mail descrita na Proposta de Valor.
 */
public enum TipoNotificacaoEnum {
    /** Enviada assim que a consulta é agendada. */
    CONFIRMACAO,

    /** Enviada antes da consulta para lembrar o paciente. */
    LEMBRETE,

    /** Enviada quando a consulta é cancelada. */
    CANCELAMENTO,

    /** Enviada quando a consulta é remarcada. */
    REMARCACAO
}
