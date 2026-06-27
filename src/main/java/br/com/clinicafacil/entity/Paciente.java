package br.com.clinicafacil.entity;

import br.com.clinicafacil.entity.enums.PerfilEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Representa o usuário do tipo Paciente no ClinicaFácil.
 *
 * Conforme o Diagrama de Classes :
 *   Atributos herdados de Usuario: id, nome, email, senha, perfil
 *   Atributos próprios: -cpf: String, -dataNascimento: Date,
 *                       -telefone: String, -historico: String
 *   Método: +getConsultas(): List<Consulta>
 *
 * Conforme o modelo do banco :
 *   Tabela única "usuario" com coluna discriminadora dtype = 'PACIENTE'.
 *
 * Herança SINGLE_TABLE: os campos específicos de Paciente ficam
 * na mesma tabela "usuario", com valor nullable para os campos
 * que não pertencem ao tipo Medico e vice-versa.
 *
 * Caso de uso: UC07 - Cadastrar Paciente (Recepcionista).
 */
@Entity
@DiscriminatorValue("PACIENTE")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true, exclude = "consultas")
public class Paciente extends Usuario {

    /** CPF do paciente — identificador único civil. */
    @Column(unique = true, length = 14)
    private String cpf;

    /** Data de nascimento para cálculo de idade e histórico. */
    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    /** Telefone para contato ou envio de notificações (WhatsApp futuro). */
    @Column(length = 20)
    private String telefone;

    /**
     * Histórico simplificado de atendimentos.
     * Conforme o Diagrama de Classes: -historico: String.
     * Em versões futuras, substituir por prontuário eletrônico completo
     */
    @Column(columnDefinition = "TEXT")
    private String historico;

    /**
     * Lista de consultas do paciente.
     * Relacionamento 1:N com Consulta (mapeado pelo campo paciente em Consulta).
     * Método: +getConsultas(): List<Consulta>
     */
    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Consulta> consultas;

    /**
     * Construtor utilizado nos testes e no service layer.
     * Repassa os campos da superclasse via super().
     */
    public Paciente(String nome, String email, String senha,
                    String cpf, LocalDate dataNascimento, String telefone) {
        super(null, nome, email, senha, PerfilEnum.PACIENTE, null);
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
    }
}
