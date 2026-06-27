package br.com.clinicafacil.controller;

import br.com.clinicafacil.dto.request.ConsultaRequestDTOs.*;
import br.com.clinicafacil.dto.response.ResponseDTOs.*;
import br.com.clinicafacil.service.ConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller REST para Consultas — peça central do ClinicaFácil.
 *
 * Implementa os endpoints mapeados no Diagrama de Sequência :
 *   POST /consultas              → UC02 Agendar
 *   PATCH /consultas/{id}/cancelar → UC03 Cancelar
 *   PATCH /consultas/{id}/remarcar → UC04 Remarcar
 *   PATCH /consultas/{id}/confirmar → confirmar presença
 *   PATCH /consultas/{id}/observacao → UC11 Registrar Observação
 *   GET /consultas/medico/{id}  → UC05 Visualizar Agenda
 *   GET /consultas/relatorio    → UC10 Gerar Relatório
 *
 * Conforme orientado na disciplina:
 *   "Uso de @RestController e @RequestMapping para definição das rotas.
 *    Métodos para listar, buscar, criar, atualizar e deletar registros."
 */
@RestController
@RequestMapping("/api/consultas")
@RequiredArgsConstructor
@Tag(name = "Consultas", description = "Agendamento e gestão de consultas (UC02, UC03, UC04, UC05, UC10, UC11)")
public class ConsultaController {

    private final ConsultaService consultaService;

    // ── UC02: Agendar ─────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Agenda uma nova consulta",
               description = "Implementa o fluxo completo do Diagrama de Sequência: " +
                             "verificarConflito() → save() → enviarNotificacao()")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Consulta agendada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Paciente ou médico não encontrado"),
            @ApiResponse(responseCode = "409", description = "Conflito de horário detectado")
    })
    public ResponseEntity<ConsultaResponse> agendar(
            @Valid @RequestBody AgendarConsultaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(consultaService.agendar(request));
    }

    // ── Listar / Buscar ───────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Lista todas as consultas")
    public ResponseEntity<List<ConsultaResponse>> listarTodas() {
        return ResponseEntity.ok(consultaService.listarTodas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca consulta pelo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta encontrada"),
            @ApiResponse(responseCode = "404", description = "Consulta não encontrada")
    })
    public ResponseEntity<ConsultaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(consultaService.buscarPorId(id));
    }

    @GetMapping("/paciente/{pacienteId}")
    @Operation(summary = "Lista o histórico de consultas de um paciente")
    public ResponseEntity<List<ConsultaResponse>> listarPorPaciente(
            @PathVariable Long pacienteId) {
        return ResponseEntity.ok(consultaService.listarPorPaciente(pacienteId));
    }

    @GetMapping("/medico/{medicoId}/agenda")
    @Operation(summary = "Visualiza a agenda do médico em um período (UC05)",
               description = "Agenda diária, semanal ou mensal. Parâmetros: inicio e fim no formato ISO 8601")
    public ResponseEntity<List<ConsultaResponse>> agendaMedico(
            @PathVariable Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        return ResponseEntity.ok(consultaService.listarPorMedicoEPeriodo(medicoId, inicio, fim));
    }

    // ── UC03: Cancelar ────────────────────────────────────────────

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancela uma consulta (UC03)",
               description = "Altera o status para CANCELADA e envia notificação ao paciente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta cancelada"),
            @ApiResponse(responseCode = "404", description = "Consulta não encontrada"),
            @ApiResponse(responseCode = "409", description = "Consulta já cancelada ou já realizada")
    })
    public ResponseEntity<ConsultaResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(consultaService.cancelar(id));
    }

    // ── UC04: Remarcar ────────────────────────────────────────────

    @PatchMapping("/{id}/remarcar")
    @Operation(summary = "Remarca uma consulta para nova data/hora (UC04)",
               description = "Verifica conflito na nova data antes de remarcar")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta remarcada"),
            @ApiResponse(responseCode = "409", description = "Conflito de horário na nova data")
    })
    public ResponseEntity<ConsultaResponse> remarcar(
            @PathVariable Long id,
            @Valid @RequestBody RemarcarConsultaRequest request) {
        return ResponseEntity.ok(consultaService.remarcar(id, request));
    }

    // ── Confirmar ─────────────────────────────────────────────────

    @PatchMapping("/{id}/confirmar")
    @Operation(summary = "Confirma a presença na consulta")
    public ResponseEntity<ConsultaResponse> confirmar(@PathVariable Long id) {
        return ResponseEntity.ok(consultaService.confirmar(id));
    }

    // ── UC11: Registrar Observação ────────────────────────────────

    @PatchMapping("/{id}/observacao")
    @Operation(summary = "Registra observação do médico e marca como REALIZADA (UC11)")
    public ResponseEntity<ConsultaResponse> registrarObservacao(
            @PathVariable Long id,
            @Valid @RequestBody ObservacaoRequest request) {
        return ResponseEntity.ok(consultaService.registrarObservacao(id, request));
    }

    // ── UC10: Relatório ───────────────────────────────────────────

    @GetMapping("/relatorio")
    @Operation(summary = "Gera relatório gerencial de ocupação e comparecimento (UC10)",
               description = "Retorna totais por status e taxa de comparecimento")
    public ResponseEntity<RelatorioResponse> relatorio() {
        return ResponseEntity.ok(consultaService.gerarRelatorio());
    }
}
