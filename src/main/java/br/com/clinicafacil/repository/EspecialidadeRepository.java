package br.com.clinicafacil.repository;

import br.com.clinicafacil.entity.Especialidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositório JPA para a entidade Especialidade.
 *
 * Conforme orientado na disciplina:
 *   "Criação dos repositórios JPA:
 *    public interface ClienteRepository extends JpaRepository<Cliente, Long> {}"
 *
 * O JpaRepository fornece automaticamente os métodos:
 *   - findAll(), findById(), save(), deleteById(), count(), existsById() etc.
 *
 * Caso de uso: UC09 - Cadastrar Especialidade (Administrador).
 */
@Repository
public interface EspecialidadeRepository extends JpaRepository<Especialidade, Long> {

    /**
     * Busca especialidade por nome (case-insensitive).
     * Útil para verificar duplicatas antes de cadastrar.
     */
    Optional<Especialidade> findByNomeIgnoreCase(String nome);

    /**
     * Verifica se já existe uma especialidade com o nome informado.
     */
    boolean existsByNomeIgnoreCase(String nome);
}
