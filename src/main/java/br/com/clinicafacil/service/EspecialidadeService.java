package br.com.clinicafacil.service;

import br.com.clinicafacil.dto.request.RequestDTOs.EspecialidadeRequest;
import br.com.clinicafacil.dto.response.ResponseDTOs.EspecialidadeResponse;
import br.com.clinicafacil.entity.Especialidade;
import br.com.clinicafacil.exception.GlobalExceptionHandler.RecursoNaoEncontradoException;
import br.com.clinicafacil.exception.GlobalExceptionHandler.RegraDeNegocioException;
import br.com.clinicafacil.repository.EspecialidadeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serviço para gerenciamento de Especialidades.
 *
 * Camada de negócio entre o Controller e o Repository,
 * seguindo a arquitetura MVC em camadas orientada na disciplina.
 *
 * Caso de uso: UC09 - Cadastrar Especialidade (Administrador).
 *
 * @Transactional garante atomicidade nas operações de escrita.
 * @Slf4j fornece o logger sem boilerplate.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EspecialidadeService {

    private final EspecialidadeRepository especialidadeRepository;

    /** Lista todas as especialidades cadastradas. */
    @Transactional(readOnly = true)
    public List<EspecialidadeResponse> listarTodas() {
        log.debug("Listando todas as especialidades");
        return especialidadeRepository.findAll()
                .stream()
                .map(EspecialidadeResponse::from)
                .toList();
    }

    /** Busca especialidade por ID. */
    @Transactional(readOnly = true)
    public EspecialidadeResponse buscarPorId(Long id) {
        return EspecialidadeResponse.from(buscarEntidadePorId(id));
    }

    /** Cria uma nova especialidade. */
    @Transactional
    public EspecialidadeResponse criar(EspecialidadeRequest request) {
        log.info("Criando especialidade: {}", request.nome());

        if (especialidadeRepository.existsByNomeIgnoreCase(request.nome())) {
            throw new RegraDeNegocioException(
                    "Já existe uma especialidade com o nome: " + request.nome());
        }

        Especialidade especialidade = Especialidade.builder()
                .nome(request.nome())
                .descricao(request.descricao())
                .build();

        Especialidade salva = especialidadeRepository.save(especialidade);
        log.info("Especialidade criada com ID: {}", salva.getId());
        return EspecialidadeResponse.from(salva);
    }

    /** Atualiza uma especialidade existente. */
    @Transactional
    public EspecialidadeResponse atualizar(Long id, EspecialidadeRequest request) {
        log.info("Atualizando especialidade ID: {}", id);
        Especialidade especialidade = buscarEntidadePorId(id);

        // Verifica duplicata apenas se o nome mudou
        if (!especialidade.getNome().equalsIgnoreCase(request.nome())
                && especialidadeRepository.existsByNomeIgnoreCase(request.nome())) {
            throw new RegraDeNegocioException(
                    "Já existe uma especialidade com o nome: " + request.nome());
        }

        especialidade.setNome(request.nome());
        especialidade.setDescricao(request.descricao());

        return EspecialidadeResponse.from(especialidadeRepository.save(especialidade));
    }

    /** Remove uma especialidade. */
    @Transactional
    public void excluir(Long id) {
        log.info("Excluindo especialidade ID: {}", id);
        Especialidade especialidade = buscarEntidadePorId(id);
        especialidadeRepository.delete(especialidade);
    }

    // ── Método interno para reutilização ──────────────────────────

    public Especialidade buscarEntidadePorId(Long id) {
        return especialidadeRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Especialidade não encontrada com ID: " + id));
    }
}
