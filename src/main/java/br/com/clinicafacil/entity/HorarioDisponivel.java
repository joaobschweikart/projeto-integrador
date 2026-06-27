package br.com.clinicafacil.entity;

import br.com.clinicafacil.entity.enums.DiaSemanaEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

/**
 * Representa um bloco de horário disponível de um médico.
 *
 * Conforme o Diagrama de Classes :
 *   Atributos: -id: Long, -diaSemana: DiaEnum,
 *              -horaInicio: Time, -horaFim: Time
 *
 * Conforme o modelo do banco :
 *   Tabela: horario_disponivel (id, medico_id, dia_semana, hora_inicio, hora_fim)
 *   Relação: N:1 com Medico (medico_id FK).
 *
 * Exemplo de uso: médico disponível toda segunda das 08:00 às 12:00.
 * Essa informação é consumida no Diagrama de Sequência 
 * para listar horários disponíveis antes do agendamento.
 */
@Entity
@Table(name = "horario_disponivel")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HorarioDisponivel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Médico ao qual este horário pertence.
     * FK: medico_id → tabela usuario (onde dtype = 'MEDICO').
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    /**
     * Dia da semana deste bloco de horário.
     * Conforme atributo "-diaSemana: DiaEnum" do Diagrama de Classes.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false, length = 10)
    private DiaSemanaEnum diaSemana;

    /**
     * Hora de início do atendimento.
     * Conforme atributo "-horaInicio: Time" do Diagrama de Classes.
     */
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    /**
     * Hora de fim do atendimento.
     * Conforme atributo "-horaFim: Time" do Diagrama de Classes.
     */
    @Column(name = "hora_fim", nullable = false)
    private LocalTime horaFim;
}
