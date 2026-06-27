package br.com.clinicafacil.controller;

import br.com.clinicafacil.dto.request.RequestDTOs.EspecialidadeRequest;
import br.com.clinicafacil.dto.response.ResponseDTOs.EspecialidadeResponse;
import br.com.clinicafacil.service.EspecialidadeService;
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
 * Controller REST para Especialidades.
 *
 * Conforme orientado na disciplina:
 *   "Uso de @RestController e @RequestMapping para definição das rotas."
 *   "Métodos para listar, buscar, criar, atualizar e deletar registros."
 *
 * Caso de uso: UC09 - Cadastrar Especialidade (Administrador).
 *
 * Endpoints disponíveis:
 *   GET    /api/especialidades       → listar todas
 *   GET    /api/especialidades/{id}  → buscar por ID
 *   POST   /api/especialidades       → criar
 *   PUT    /api/especialidades/{id}  → atualizar
 *   DELETE /api/especialidades/{id}  → excluir
 */
@RestController
@RequestMapping("/api/especialidades")
@RequiredArgsConstructor
@Tag(name = "Especialidades", description = "Gerenciamento de especialidades médicas (UC09)")
public class EspecialidadeController {

    private final EspecialidadeService especialidadeService;

    @GetMapping
    @Operation(summary = "Lista todas as especialidades cadastradas")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public ResponseEntity<List<EspecialidadeResponse>> listarTodas() {
        return ResponseEntity.ok(especialidadeService.listarTodas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma especialidade pelo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Especialidade encontrada"),
            @ApiResponse(responseCode = "404", description = "Especialidade não encontrada")
    })
    public ResponseEntity<EspecialidadeResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(especialidadeService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cadastra uma nova especialidade")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Especialidade criada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Especialidade já cadastrada")
    })
    public ResponseEntity<EspecialidadeResponse> criar(
            @Valid @RequestBody EspecialidadeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(especialidadeService.criar(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma especialidade existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Especialidade não encontrada")
    })
    public ResponseEntity<EspecialidadeResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody EspecialidadeRequest request) {
        return ResponseEntity.ok(especialidadeService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma especialidade")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Especialidade não encontrada")
    })
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        especialidadeService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
