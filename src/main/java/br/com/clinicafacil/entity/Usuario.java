package br.com.clinicafacil.entity;

import br.com.clinicafacil.entity.enums.PerfilEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidade base do sistema, conforme o Diagrama de Classes .
 *
 * Atributos mapeados:
 *   -id: Long, -nome: String, -email: String, -senha: String, -perfil: PerfilEnum
 *
 * Métodos modelados:
 *   +autenticar(): boolean, +alterarSenha(novaSenha: String): void
 *
 * Estratégia de herança: SINGLE_TABLE
 * Uma única tabela "usuario" armazena todos os subtipos (Paciente, Medico).
 * A coluna discriminadora "dtype" identifica o tipo real de cada linha.
 * Vantagem: simplicidade, sem JOINs na consulta.
 *
 * Conforme orientado na disciplina:
 *   @Entity, @Table, @Id, @GeneratedValue são as anotações fundamentais do JPA.
 */
@Entity
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "senha")
public abstract class Usuario {

    /** Chave primária gerada automaticamente pelo banco (AUTO_INCREMENT). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nome completo do usuário. */
    @Column(nullable = false, length = 150)
    private String nome;

    /** E-mail único — utilizado para login e envio de notificações. */
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /** Senha armazenada como hash (ex.: BCrypt em produção). */
    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senha;

    /**
     * Perfil de acesso do usuário.
     * Armazenado como String no banco para legibilidade.
     * Conforme os perfis descritos na seção 1.2 do projeto .
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PerfilEnum perfil;

    /** Data/hora de criação do registro. Preenchida automaticamente. */
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    /**
     * Preenche criadoEm antes do primeiro INSERT.
     * Equivalente ao campo "criado_em datetime" do modelo do banco .
     */
    @PrePersist
    protected void prePersist() {
        this.criadoEm = LocalDateTime.now();
    }

    /**
     * Simula a autenticação do usuário comparando senhas.
     * Em produção, substituir pela comparação com BCrypt.
     * Método conforme Diagrama de Classes: +autenticar(): boolean
     */
    public boolean autenticar(String senhaInformada) {
        return this.senha != null && this.senha.equals(senhaInformada);
    }

    /**
     * Altera a senha do usuário.
     * Método conforme Diagrama de Classes: +alterarSenha(novaSenha: String): void
     */
    public void alterarSenha(String novaSenha) {
        this.senha = novaSenha;
    }
}
