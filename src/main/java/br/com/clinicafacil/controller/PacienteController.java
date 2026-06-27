package br.com.clinicafacil.controller;

import br.com.clinicafacil.dto.request.RequestDTOs.PacienteRequest;
import br.com.clinicafacil.dto.response.ResponseDTOs.PacienteResponse;
import br.com.clinicafacil.service.PacienteService;
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
 * Controller REST para Pacientes.
 *
 * Caso de uso: UC07 - Cadastrar Paciente (Recepcionista).
 *
 * Endpoints:
 *   GET    /api/pacientes           → listar todos
 *   GET    /api/pacientes/{id}      → buscar por ID
 *   GET    /api/pacientes/busca     → buscar por nome (?nome=...)
 *   POST   /api/pacientes           → cadastrar
 *   PUT    /api/pacientes/{id}      → atualizar
 *   DELETE /api/pacientes/{id}      → excluir
 */
@RestController
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
@Tag(name = "Pacientes", description = "Gerenciamento de pacientes (UC07)")
public class PacienteController {

    private final PacienteService pacienteService;

    @GetMapping
    @Operation(summary = "Lista todos os pacientes cadastrados")
    public ResponseEntity<List<PacienteResponse>> listarTodos() {
        return ResponseEntity.ok(pacienteService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca paciente pelo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paciente encontrado"),
            @ApiResponse(responseCode = "404", description = "Paciente não encontrado")
    })
    public ResponseEntity<PacienteResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.buscarPorId(id));
    }

    @GetMapping("/busca")
    @Operation(summary = "Busca pacientes pelo nome (parcial, case-insensitive)")
    public ResponseEntity<List<PacienteResponse>> buscarPorNome(
            @RequestParam String nome) {
        return ResponseEntity.ok(pacienteService.buscarPorNome(nome));
    }

    @PostMapping
    @Operation(summary = "Cadastra um novo paciente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Paciente cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "CPF ou e-mail já cadastrado")
    })
    public ResponseEntity<PacienteResponse> criar(
            @Valid @RequestBody PacienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pacienteService.criar(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza dados de um paciente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Paciente não encontrado")
    })
    public ResponseEntity<PacienteResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody PacienteRequest request) {
        return ResponseEntity.ok(pacienteService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um paciente do sistema")
    @ApiResponse(responseCode = "204", description = "Excluído com sucesso")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        pacienteService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
