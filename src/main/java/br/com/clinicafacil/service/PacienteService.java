package br.com.clinicafacil.service;

import br.com.clinicafacil.dto.request.RequestDTOs.PacienteRequest;
import br.com.clinicafacil.dto.response.ResponseDTOs.PacienteResponse;
import br.com.clinicafacil.entity.Paciente;
import br.com.clinicafacil.entity.enums.PerfilEnum;
import br.com.clinicafacil.exception.GlobalExceptionHandler.RecursoNaoEncontradoException;
import br.com.clinicafacil.exception.GlobalExceptionHandler.RegraDeNegocioException;
import br.com.clinicafacil.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serviço para gerenciamento de Pacientes.
 *
 * Caso de uso: UC07 - Cadastrar Paciente (Recepcionista).
 *
 * Regras de negócio aplicadas nesta camada (a validação ocorre
 * no Service, antes da persistência, garantindo integridade dos dados):
 *   - CPF único por paciente
 *   - E-mail único no sistema
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    /** Lista todos os pacientes. */
    @Transactional(readOnly = true)
    public List<PacienteResponse> listarTodos() {
        return pacienteRepository.findAll()
                .stream()
                .map(PacienteResponse::from)
                .toList();
    }

    /** Busca paciente por ID. */
    @Transactional(readOnly = true)
    public PacienteResponse buscarPorId(Long id) {
        return PacienteResponse.from(buscarEntidadePorId(id));
    }

    /** Busca pacientes pelo nome (busca parcial). */
    @Transactional(readOnly = true)
    public List<PacienteResponse> buscarPorNome(String nome) {
        return pacienteRepository.findByNomeContainingIgnoreCase(nome)
                .stream()
                .map(PacienteResponse::from)
                .toList();
    }

    /** Cadastra um novo paciente. */
    @Transactional
    public PacienteResponse criar(PacienteRequest request) {
        log.info("Cadastrando paciente: {}", request.nome());

        // Validação: CPF único
        if (pacienteRepository.existsByCpf(request.cpf())) {
            throw new RegraDeNegocioException(
                    "Já existe um paciente cadastrado com o CPF: " + request.cpf());
        }

        // Validação: e-mail único
        if (pacienteRepository.existsByEmail(request.email())) {
            throw new RegraDeNegocioException(
                    "Já existe um usuário cadastrado com o e-mail: " + request.email());
        }

        Paciente paciente = new Paciente(
                request.nome(), request.email(), request.senha(),
                request.cpf(), request.dataNascimento(), request.telefone()
        );

        Paciente salvo = pacienteRepository.save(paciente);
        log.info("Paciente cadastrado com ID: {}", salvo.getId());
        return PacienteResponse.from(salvo);
    }

    /** Atualiza dados de um paciente existente. */
    @Transactional
    public PacienteResponse atualizar(Long id, PacienteRequest request) {
        log.info("Atualizando paciente ID: {}", id);
        Paciente paciente = buscarEntidadePorId(id);

        // Valida CPF apenas se mudou
        if (!paciente.getCpf().equals(request.cpf())
                && pacienteRepository.existsByCpf(request.cpf())) {
            throw new RegraDeNegocioException(
                    "Já existe um paciente com o CPF: " + request.cpf());
        }

        // Valida e-mail apenas se mudou
        if (!paciente.getEmail().equalsIgnoreCase(request.email())
                && pacienteRepository.existsByEmail(request.email())) {
            throw new RegraDeNegocioException(
                    "Já existe um usuário com o e-mail: " + request.email());
        }

        paciente.setNome(request.nome());
        paciente.setEmail(request.email());
        paciente.setCpf(request.cpf());
        paciente.setDataNascimento(request.dataNascimento());
        paciente.setTelefone(request.telefone());

        // Atualiza senha apenas se fornecida
        if (request.senha() != null && !request.senha().isBlank()) {
            paciente.setSenha(request.senha());
        }

        return PacienteResponse.from(pacienteRepository.save(paciente));
    }

    /** Remove um paciente do sistema. */
    @Transactional
    public void excluir(Long id) {
        log.info("Excluindo paciente ID: {}", id);
        pacienteRepository.delete(buscarEntidadePorId(id));
    }

    // ── Métodos internos ──────────────────────────────────────────

    public Paciente buscarEntidadePorId(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Paciente não encontrado com ID: " + id));
    }
}
