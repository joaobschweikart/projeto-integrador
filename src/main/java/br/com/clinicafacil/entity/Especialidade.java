package br.com.clinicafacil.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Representa uma especialidade médica (ex.: Cardiologia, Ortopedia).
 *
 * Conforme o Diagrama de Classes :
 *   Atributos: -id: Long, -nome: String, -descricao: String
 *
 * Conforme o modelo do banco :
 *   Tabela: especialidade (id, nome, descricao)
 *
 * Relacionamento: 1 Especialidade → N Medicos (mapeado em Medico).
 * Caso de uso coberto: UC09 - Cadastrar Especialidade (Administrador).
 */
@Entity
@Table(name = "especialidade")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "medicos")
public class Especialidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nome da especialidade (ex.: Cardiologia). */
    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    /** Descrição opcional da especialidade. */
    @Column(length = 255)
    private String descricao;

    /**
     * Lista de médicos desta especialidade.
     * mappedBy indica que a FK está na tabela "medico".
     * Carregamento LAZY para evitar consultas desnecessárias.
     */
    @OneToMany(mappedBy = "especialidade", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Medico> medicos;
}
