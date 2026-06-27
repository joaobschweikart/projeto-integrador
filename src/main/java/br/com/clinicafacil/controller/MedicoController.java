package br.com.clinicafacil.controller;

import br.com.clinicafacil.dto.request.RequestDTOs.HorarioDisponivelRequest;
import br.com.clinicafacil.dto.request.RequestDTOs.MedicoRequest;
import br.com.clinicafacil.dto.response.ResponseDTOs.HorarioDisponivelResponse;
import br.com.clinicafacil.dto.response.ResponseDTOs.MedicoResponse;
import br.com.clinicafacil.service.MedicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para Médicos e Horários Disponíveis.
 *
 * Casos de uso: UC08 - Cadastrar Medico, UC05 - Visualizar Agenda.
 *
 * Conforme o Diagrama de Sequência :
 *   GET /medicos?especialidade=X&data=Y
 *   → listarHorariosDisponiveis(esp, data)
 *
 * Endpoints de Médico:
 *   GET    /api/medicos                            → listar todos
 *   GET    /api/medicos/{id}                       → buscar por ID
 *   GET    /api/medicos/especialidade/{id}          → listar por especialidade
 *   POST   /api/medicos                            → cadastrar
 *   PUT    /api/medicos/{id}                       → atualizar
 *   DELETE /api/medicos/{id}                       → excluir
 *
 * Endpoints de Horário:
 *   GET    /api/medicos/{id}/horarios              → listar horários do médico
 *   POST   /api/medicos/horarios                   → adicionar horário
 *   DELETE /api/medicos/horarios/{horarioId}        → remover horário
 */
@RestController
@RequestMapping("/api/medicos")
@RequiredArgsConstructor
@Tag(name = "Médicos", description = "Gerenciamento de médicos e horários (UC08, UC05)")
public class MedicoController {

    private final MedicoService medicoService;

    // ── Médico ────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Lista todos os médicos cadastrados")
    public ResponseEntity<List<MedicoResponse>> listarTodos() {
        return ResponseEntity.ok(medicoService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca médico pelo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Médico encontrado"),
            @ApiResponse(responseCode = "404", description = "Médico não encontrado")
    })
    public ResponseEntity<MedicoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(medicoService.buscarPorId(id));
    }

    @GetMapping("/especialidade/{especialidadeId}")
    @Operation(summary = "Lista médicos disponíveis por especialidade",
               description = "Conforme Diagrama de Sequência: GET /medicos?especialidade=X&data=Y")
    public ResponseEntity<List<MedicoResponse>> listarPorEspecialidade(
            @PathVariable Long especialidadeId) {
        return ResponseEntity.ok(medicoService.listarPorEspecialidade(especialidadeId));
    }

    @PostMapping
    @Operation(summary = "Cadastra um novo médico")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Médico cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "CRM já cadastrado")
    })
    public ResponseEntity<MedicoResponse> criar(@Valid @RequestBody MedicoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicoService.criar(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza dados de um médico")
    public ResponseEntity<MedicoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody MedicoRequest request) {
        return ResponseEntity.ok(medicoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um médico do sistema")
    @ApiResponse(responseCode = "204", description = "Excluído com sucesso")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        medicoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // ── Horários Disponíveis ──────────────────────────────────────

    @GetMapping("/{medicoId}/horarios")
    @Operation(summary = "Lista horários disponíveis de um médico",
               description = "Implementa SELECT horarios livres do Diagrama de Sequência")
    public ResponseEntity<List<HorarioDisponivelResponse>> listarHorarios(
            @PathVariable Long medicoId) {
        return ResponseEntity.ok(medicoService.listarHorariosPorMedico(medicoId));
    }

    @PostMapping("/horarios")
    @Operation(summary = "Adiciona horário de atendimento a um médico")
    @ApiResponse(responseCode = "201", description = "Horário adicionado")
    public ResponseEntity<HorarioDisponivelResponse> adicionarHorario(
            @Valid @RequestBody HorarioDisponivelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(medicoService.adicionarHorario(request));
    }

    @DeleteMapping("/horarios/{horarioId}")
    @Operation(summary = "Remove um horário disponível")
    @ApiResponse(responseCode = "204", description = "Horário removido")
    public ResponseEntity<Void> removerHorario(@PathVariable Long horarioId) {
        medicoService.removerHorario(horarioId);
        return ResponseEntity.noContent().build();
    }
}
