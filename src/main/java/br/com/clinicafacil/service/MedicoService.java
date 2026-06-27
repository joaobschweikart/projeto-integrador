package br.com.clinicafacil.service;

import br.com.clinicafacil.dto.request.RequestDTOs.HorarioDisponivelRequest;
import br.com.clinicafacil.dto.request.RequestDTOs.MedicoRequest;
import br.com.clinicafacil.dto.response.ResponseDTOs.HorarioDisponivelResponse;
import br.com.clinicafacil.dto.response.ResponseDTOs.MedicoResponse;
import br.com.clinicafacil.entity.HorarioDisponivel;
import br.com.clinicafacil.entity.Medico;
import br.com.clinicafacil.exception.GlobalExceptionHandler.RecursoNaoEncontradoException;
import br.com.clinicafacil.exception.GlobalExceptionHandler.RegraDeNegocioException;
import br.com.clinicafacil.repository.HorarioDisponivelRepository;
import br.com.clinicafacil.repository.MedicoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serviço para gerenciamento de Médicos e seus Horários Disponíveis.
 *
 * Casos de uso cobertos:
 *   UC08 - Cadastrar Medico (Administrador)
 *   UC05 - Visualizar Agenda (Médico)
 *
 * Conforme o Diagrama de Sequência :
 *   GET /medicos?especialidade=X → listarHorariosDisponiveis(esp, data)
 *   → findDisponivel(esp, data) → SELECT horarios livres
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MedicoService {

    private final MedicoRepository medicoRepository;
    private final HorarioDisponivelRepository horarioDisponivelRepository;
    private final EspecialidadeService especialidadeService;

    // ── CRUD de Médico ────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MedicoResponse> listarTodos() {
        return medicoRepository.findAll().stream().map(MedicoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public MedicoResponse buscarPorId(Long id) {
        return MedicoResponse.from(buscarEntidadePorId(id));
    }

    @Transactional(readOnly = true)
    public List<MedicoResponse> listarPorEspecialidade(Long especialidadeId) {
        return medicoRepository.findDisponiveisPorEspecialidade(especialidadeId)
                .stream()
                .map(MedicoResponse::from)
                .toList();
    }

    @Transactional
    public MedicoResponse criar(MedicoRequest request) {
        log.info("Cadastrando médico: {} | CRM: {}", request.nome(), request.crm());

        if (medicoRepository.existsByCrm(request.crm())) {
            throw new RegraDeNegocioException(
                    "Já existe um médico cadastrado com o CRM: " + request.crm());
        }

        var especialidade = especialidadeService.buscarEntidadePorId(request.especialidadeId());

        Medico medico = new Medico(
                request.nome(), request.email(), request.senha(),
                request.crm(), especialidade
        );

        Medico salvo = medicoRepository.save(medico);
        log.info("Médico cadastrado com ID: {}", salvo.getId());
        return MedicoResponse.from(salvo);
    }

    @Transactional
    public MedicoResponse atualizar(Long id, MedicoRequest request) {
        log.info("Atualizando médico ID: {}", id);
        Medico medico = buscarEntidadePorId(id);

        if (!medico.getCrm().equals(request.crm())
                && medicoRepository.existsByCrm(request.crm())) {
            throw new RegraDeNegocioException("CRM já cadastrado: " + request.crm());
        }

        var especialidade = especialidadeService.buscarEntidadePorId(request.especialidadeId());

        medico.setNome(request.nome());
        medico.setEmail(request.email());
        medico.setCrm(request.crm());
        medico.setEspecialidade(especialidade);

        if (request.senha() != null && !request.senha().isBlank()) {
            medico.setSenha(request.senha());
        }

        return MedicoResponse.from(medicoRepository.save(medico));
    }

    @Transactional
    public void excluir(Long id) {
        log.info("Excluindo médico ID: {}", id);
        medicoRepository.delete(buscarEntidadePorId(id));
    }

    // ── CRUD de Horário Disponível ────────────────────────────────

    /**
     * Lista horários disponíveis de um médico.
     * Conforme o Diagrama de Sequência :
     *   findDisponivel(esp, data) → SELECT horarios livres → horarios
     */
    @Transactional(readOnly = true)
    public List<HorarioDisponivelResponse> listarHorariosPorMedico(Long medicoId) {
        buscarEntidadePorId(medicoId); // valida existência
        return horarioDisponivelRepository.findByMedicoId(medicoId)
                .stream()
                .map(HorarioDisponivelResponse::from)
                .toList();
    }

    @Transactional
    public HorarioDisponivelResponse adicionarHorario(HorarioDisponivelRequest request) {
        log.info("Adicionando horário para médico ID: {}", request.medicoId());
        Medico medico = buscarEntidadePorId(request.medicoId());

        HorarioDisponivel horario = HorarioDisponivel.builder()
                .medico(medico)
                .diaSemana(request.diaSemana())
                .horaInicio(request.horaInicio())
                .horaFim(request.horaFim())
                .build();

        return HorarioDisponivelResponse.from(horarioDisponivelRepository.save(horario));
    }

    @Transactional
    public void removerHorario(Long horarioId) {
        HorarioDisponivel horario = horarioDisponivelRepository.findById(horarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Horário não encontrado com ID: " + horarioId));
        horarioDisponivelRepository.delete(horario);
    }

    // ── Método interno ────────────────────────────────────────────

    public Medico buscarEntidadePorId(Long id) {
        return medicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Médico não encontrado com ID: " + id));
    }
}
