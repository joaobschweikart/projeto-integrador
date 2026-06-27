package br.com.clinicafacil.entity;

import br.com.clinicafacil.entity.enums.StatusConsultaEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidade central do ClinicaFácil — representa uma consulta médica.
 *
 * Conforme o Diagrama de Classes :
 *   Atributos: -id: Long, -dataHora: DateTime, -status: StatusEnum,
 *              -observacao: String
 *   Métodos: +confirmar(): void, +cancelar(): void,
 *            +remarcar(novaData: DateTime): void
 *
 * Conforme o modelo do banco :
 *   Tabela: consulta (id, paciente_id, medico_id, data_hora,
 *                     status, observacao, criado_em)
 *
 * Conforme o Diagrama de Sequência :
 *   O agendamento passa por: verificarConflito() → save() → enviarNotificacao().
 *   A verificação de conflito é feita no ConsultaService (camada de serviço),
 *   antes da persistência — garantindo integridade dos dados.
 *
 * Casos de uso cobertos:
 *   UC02 - Agendar Consulta, UC03 - Cancelar Consulta, UC04 - Remarcar Consulta.
 */
@Entity
@Table(name = "consulta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"paciente", "medico", "notificacoes"})
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Paciente que agendou a consulta.
     * FK: paciente_id → tabela usuario (dtype = 'PACIENTE').
     * Relacionamento N:1 — um paciente pode ter várias consultas.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    /**
     * Médico responsável pela consulta.
     * FK: medico_id → tabela usuario (dtype = 'MEDICO').
     * Relacionamento N:1 — um médico pode ter várias consultas.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    /**
     * Data e hora da consulta.
     * Conforme atributo "-dataHora: DateTime" do Diagrama de Classes.
     * Utilizada para verificação de conflitos (Diagrama de Sequência).
     */
    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    /**
     * Status atual da consulta.
     * Conforme o atributo "-status: StatusEnum" do Diagrama de Classes.
     * Inicia como AGENDADA e transita pelos estados do StatusConsultaEnum.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusConsultaEnum status = StatusConsultaEnum.AGENDADA;

    /**
     * Observações do médico sobre a consulta.
     * Conforme atributo "-observacao: String" e UC11 - Registrar Observação.
     */
    @Column(columnDefinition = "TEXT")
    private String observacao;

    /** Timestamp de criação do registro. */
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    /**
     * Notificações geradas a partir desta consulta.
     * Relacionamento 1:N — uma consulta pode gerar várias notificações
     * (confirmação, lembrete, cancelamento...).
     * Conforme modelo do banco: notificacao.consulta_id FK.
     */
    @OneToMany(mappedBy = "consulta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Notificacao> notificacoes;

    @PrePersist
    protected void prePersist() {
        this.criadoEm = LocalDateTime.now();
        if (this.status == null) {
            this.status = StatusConsultaEnum.AGENDADA;
        }
    }

    /**
     * Confirma a consulta.
     * Método: +confirmar(): void (Diagrama de Classes).
     */
    public void confirmar() {
        this.status = StatusConsultaEnum.CONFIRMADA;
    }

    /**
     * Cancela a consulta.
     * Método: +cancelar(): void (Diagrama de Classes).
     * Caso de uso: UC03 - Cancelar Consulta.
     */
    public void cancelar() {
        this.status = StatusConsultaEnum.CANCELADA;
    }

    /**
     * Remarca a consulta para uma nova data/hora.
     * Método: +remarcar(novaData: DateTime): void (Diagrama de Classes).
     * Caso de uso: UC04 - Remarcar Consulta.
     */
    public void remarcar(LocalDateTime novaData) {
        this.dataHora = novaData;
        this.status = StatusConsultaEnum.AGENDADA;
    }
}
