package br.com.clinicafacil.entity;

import br.com.clinicafacil.entity.enums.PerfilEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Representa o usuário do tipo Médico no ClinicaFácil.
 *
 * Conforme o Diagrama de Classes :
 *   Atributos: -crm: String, -especialidade: Especialidade,
 *              -horarioAtendimento: List<HorarioDisponivel>
 *   Método: +getAgenda(data: Date): List<Consulta>
 *
 * Conforme o modelo do banco :
 *   Tabela única "usuario" (SINGLE_TABLE), dtype = 'MEDICO'.
 *   FK: especialidade_id → tabela especialidade.
 *
 * Caso de uso: UC08 - Cadastrar Medico (Administrador).
 */
@Entity
@DiscriminatorValue("MEDICO")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true, exclude = {"horariosAtendimento", "consultas"})
public class Medico extends Usuario {

    /**
     * Número do CRM do médico — identificador profissional obrigatório.
     * Conforme atributo "-crm: String" do Diagrama de Classes.
     */
    @Column(unique = true, length = 20)
    private String crm;

    /**
     * Especialidade médica do profissional.
     * Relacionamento N:1 — vários médicos podem ter a mesma especialidade.
     * Conforme o modelo do banco: FK especialidade_id.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "especialidade_id")
    private Especialidade especialidade;

    /**
     * Horários disponíveis para atendimento.
     * Relacionamento 1:N — um médico tem vários horários disponíveis.
     * Conforme o Diagrama de Classes: -horarioAtendimento: List<HorarioDisponivel>.
     * Conforme modelo do banco: relação 1:N (medico → horario_disponivel).
     */
    @OneToMany(mappedBy = "medico", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HorarioDisponivel> horariosAtendimento;

    /**
     * Consultas agendadas para este médico.
     * Relacionamento 1:N — um médico possui várias consultas.
     */
    @OneToMany(mappedBy = "medico", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Consulta> consultas;

    /**
     * Construtor conveniente para uso no service e testes.
     */
    public Medico(String nome, String email, String senha, String crm, Especialidade especialidade) {
        super(null, nome, email, senha, PerfilEnum.MEDICO, null);
        this.crm = crm;
        this.especialidade = especialidade;
    }

    /**
     * Retorna as consultas do médico em uma data específica.
     * Método: +getAgenda(data: Date): List<Consulta>
     * Implementação completa no MedicoService.
     */
    public List<Consulta> getAgenda(LocalDate data) {
        if (this.consultas == null) return List.of();
        return this.consultas.stream()
                .filter(c -> c.getDataHora() != null &&
                             c.getDataHora().toLocalDate().equals(data))
                .toList();
    }
}
