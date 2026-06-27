package br.com.clinicafacil.service;

import br.com.clinicafacil.dto.request.ConsultaRequestDTOs.*;
import br.com.clinicafacil.dto.response.ResponseDTOs.*;
import br.com.clinicafacil.entity.Consulta;
import br.com.clinicafacil.entity.Notificacao;
import br.com.clinicafacil.entity.enums.StatusConsultaEnum;
import br.com.clinicafacil.entity.enums.TipoNotificacaoEnum;
import br.com.clinicafacil.exception.GlobalExceptionHandler.RecursoNaoEncontradoException;
import br.com.clinicafacil.exception.GlobalExceptionHandler.RegraDeNegocioException;
import br.com.clinicafacil.repository.ConsultaRepository;
import br.com.clinicafacil.repository.NotificacaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Serviço central do ClinicaFácil — gerencia o ciclo completo de Consultas.
 *
 * Implementa fielmente o Diagrama de Sequência :
 *   Paciente → Interface Web → ConsultaController → ConsultaService
 *   → verificarConflito() → ConsultaRepository → save() → enviarNotificacao()
 *
 * A verificação de conflito é feita NA CAMADA DE SERVIÇO, antes da
 * persistência, garantindo a integridade dos dados conforme descrito
 * na seção 2.1.3 do projeto :
 *   "A verificação de conflitos é realizada na camada de serviço,
 *    antes da persistência, garantindo a integridade dos dados."
 *
 * Casos de uso cobertos:
 *   UC02 - Agendar Consulta
 *   UC03 - Cancelar Consulta
 *   UC04 - Remarcar Consulta
 *   UC10 - Gerar Relatório
 *   UC11 - Registrar Observação
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final PacienteService pacienteService;
    private final MedicoService medicoService;

    /**
     * Status que representam consulta "ativa" — usados na verificação de conflito.
     * Apenas consultas AGENDADAS ou CONFIRMADAS bloqueiam um horário.
     */
    private static final List<StatusConsultaEnum> STATUS_ATIVOS =
            List.of(StatusConsultaEnum.AGENDADA, StatusConsultaEnum.CONFIRMADA);

    // ── UC02: Agendar Consulta ────────────────────────────────────

    /**
     * Agendamento completo conforme Diagrama de Sequência :
     * 1. Busca paciente e médico
     * 2. Verifica conflito de horário
     * 3. Persiste a consulta
     * 4. Envia notificação de confirmação
     */
    @Transactional
    public ConsultaResponse agendar(AgendarConsultaRequest request) {
        log.info("Agendando consulta: paciente={}, medico={}, dataHora={}",
                request.pacienteId(), request.medicoId(), request.dataHora());

        var paciente = pacienteService.buscarEntidadePorId(request.pacienteId());
        var medico = medicoService.buscarEntidadePorId(request.medicoId());

        // Regra de negócio central: verificação de conflito de horário
        // Conforme seção 1.3 : "eliminação de conflitos de horário
        // por meio de agenda digital em tempo real"
        verificarConflito(medico.getId(), request.dataHora(), null);

        Consulta consulta = Consulta.builder()
                .paciente(paciente)
                .medico(medico)
                .dataHora(request.dataHora())
                .status(StatusConsultaEnum.AGENDADA)
                .build();

        Consulta salva = consultaRepository.save(consulta);
        log.info("Consulta agendada com ID: {}", salva.getId());

        // Envia notificação de confirmação (UC06 - Receber Notificação)
        enviarNotificacao(salva, TipoNotificacaoEnum.CONFIRMACAO,
                String.format("Consulta confirmada com Dr(a). %s em %s",
                        medico.getNome(), request.dataHora()));

        return ConsultaResponse.from(salva);
    }

    // ── UC03: Cancelar Consulta ───────────────────────────────────

    @Transactional
    public ConsultaResponse cancelar(Long consultaId) {
        log.info("Cancelando consulta ID: {}", consultaId);
        Consulta consulta = buscarEntidadePorId(consultaId);

        if (consulta.getStatus() == StatusConsultaEnum.CANCELADA) {
            throw new RegraDeNegocioException("Consulta já está cancelada.");
        }

        if (consulta.getStatus() == StatusConsultaEnum.REALIZADA) {
            throw new RegraDeNegocioException("Não é possível cancelar uma consulta já realizada.");
        }

        consulta.cancelar();
        Consulta atualizada = consultaRepository.save(consulta);

        // Notifica o paciente sobre o cancelamento
        enviarNotificacao(atualizada, TipoNotificacaoEnum.CANCELAMENTO,
                "Sua consulta foi cancelada. Entre em contato para reagendamento.");

        return ConsultaResponse.from(atualizada);
    }

    // ── UC04: Remarcar Consulta ───────────────────────────────────

    @Transactional
    public ConsultaResponse remarcar(Long consultaId, RemarcarConsultaRequest request) {
        log.info("Remarcando consulta ID: {} para {}", consultaId, request.novaDataHora());
        Consulta consulta = buscarEntidadePorId(consultaId);

        if (consulta.getStatus() == StatusConsultaEnum.CANCELADA) {
            throw new RegraDeNegocioException("Não é possível remarcar uma consulta cancelada.");
        }

        if (consulta.getStatus() == StatusConsultaEnum.REALIZADA) {
            throw new RegraDeNegocioException("Não é possível remarcar uma consulta já realizada.");
        }

        // Verifica conflito excluindo a própria consulta da checagem
        verificarConflitoParaRemarcacao(
                consulta.getMedico().getId(), request.novaDataHora(), consultaId);

        consulta.remarcar(request.novaDataHora());
        Consulta atualizada = consultaRepository.save(consulta);

        // Notifica remarcação
        enviarNotificacao(atualizada, TipoNotificacaoEnum.REMARCACAO,
                String.format("Sua consulta foi remarcada para %s", request.novaDataHora()));

        return ConsultaResponse.from(atualizada);
    }

    // ── UC01: Confirmar Consulta ──────────────────────────────────

    @Transactional
    public ConsultaResponse confirmar(Long consultaId) {
        log.info("Confirmando consulta ID: {}", consultaId);
        Consulta consulta = buscarEntidadePorId(consultaId);

        if (consulta.getStatus() != StatusConsultaEnum.AGENDADA) {
            throw new RegraDeNegocioException(
                    "Apenas consultas AGENDADAS podem ser confirmadas. Status atual: "
                            + consulta.getStatus());
        }

        consulta.confirmar();
        return ConsultaResponse.from(consultaRepository.save(consulta));
    }

    // ── UC11: Registrar Observação ────────────────────────────────

    @Transactional
    public ConsultaResponse registrarObservacao(Long consultaId, ObservacaoRequest request) {
        log.info("Registrando observação na consulta ID: {}", consultaId);
        Consulta consulta = buscarEntidadePorId(consultaId);
        consulta.setObservacao(request.observacao());
        consulta.setStatus(StatusConsultaEnum.REALIZADA);
        return ConsultaResponse.from(consultaRepository.save(consulta));
    }

    // ── UC05: Visualizar Agenda ───────────────────────────────────

    @Transactional(readOnly = true)
    public List<ConsultaResponse> listarPorMedicoEPeriodo(
            Long medicoId, LocalDateTime inicio, LocalDateTime fim) {
        medicoService.buscarEntidadePorId(medicoId); // valida existência
        return consultaRepository.findByMedicoIdAndPeriodo(medicoId, inicio, fim)
                .stream()
                .map(ConsultaResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConsultaResponse> listarPorPaciente(Long pacienteId) {
        pacienteService.buscarEntidadePorId(pacienteId); // valida existência
        return consultaRepository.findByPacienteIdOrderByDataHoraDesc(pacienteId)
                .stream()
                .map(ConsultaResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConsultaResponse> listarTodas() {
        return consultaRepository.findAll()
                .stream()
                .map(ConsultaResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ConsultaResponse buscarPorId(Long id) {
        return ConsultaResponse.from(buscarEntidadePorId(id));
    }

    // ── UC10: Gerar Relatório ─────────────────────────────────────

    /**
     * Gera relatório gerencial de ocupação e comparecimento.
     * Conforme seção 1.3 :
     *   "Geração de relatórios de ocupação de agenda e taxa de comparecimento"
     *   "Painel administrativo com indicadores de desempenho da clínica"
     */
    @Transactional(readOnly = true)
    public RelatorioResponse gerarRelatorio() {
        long agendadas   = consultaRepository.countByStatus(StatusConsultaEnum.AGENDADA);
        long confirmadas = consultaRepository.countByStatus(StatusConsultaEnum.CONFIRMADA);
        long realizadas  = consultaRepository.countByStatus(StatusConsultaEnum.REALIZADA);
        long canceladas  = consultaRepository.countByStatus(StatusConsultaEnum.CANCELADA);
        long naoCompareceu = consultaRepository.countByStatus(StatusConsultaEnum.NAO_COMPARECEU);

        long total = agendadas + confirmadas + realizadas + canceladas + naoCompareceu;
        double taxa = total > 0 ? (double) realizadas / total * 100 : 0.0;

        return new RelatorioResponse(total, agendadas, confirmadas,
                realizadas, canceladas, naoCompareceu,
                Math.round(taxa * 10.0) / 10.0);
    }

    // ── Métodos privados ──────────────────────────────────────────

    /**
     * Verificação de conflito de horário — regra de negócio central.
     * Implementa o passo "verificarConflito()" do Diagrama de Sequência.
     */
    private void verificarConflito(Long medicoId, LocalDateTime dataHora, Long consultaIdExcluir) {
        boolean temConflito = consultaRepository.existeConflito(medicoId, dataHora, STATUS_ATIVOS);

        if (temConflito) {
            throw new RegraDeNegocioException(
                    "Conflito de horário: o médico já possui consulta agendada para " + dataHora);
        }
    }

    private void verificarConflitoParaRemarcacao(
            Long medicoId, LocalDateTime dataHora, Long consultaId) {
        boolean temConflito = consultaRepository.existeConflitoExcluindoConsulta(
                medicoId, dataHora, STATUS_ATIVOS, consultaId);

        if (temConflito) {
            throw new RegraDeNegocioException(
                    "Conflito de horário: o médico já possui consulta agendada para " + dataHora);
        }
    }

    /**
     * Envia notificação após operação na consulta.
     * Implementa o passo "enviarNotificacao()" do Diagrama de Sequência .
     * Conforme a Proposta de Valor: "redução de faltas por meio de notificações automáticas".
     */
    private void enviarNotificacao(Consulta consulta, TipoNotificacaoEnum tipo, String mensagem) {
        Notificacao notificacao = Notificacao.builder()
                .consulta(consulta)
                .tipo(tipo)
                .mensagem(mensagem)
                .build();

        notificacao.enviar(); // Método da entidade (Diagrama de Classes)
        notificacaoRepository.save(notificacao);
        log.debug("Notificação {} registrada para consulta ID: {}", tipo, consulta.getId());
    }

    public Consulta buscarEntidadePorId(Long id) {
        return consultaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Consulta não encontrada com ID: " + id));
    }
}
