package br.com.clinicafacil.controller;

import br.com.clinicafacil.dto.request.RequestDTOs.EspecialidadeRequest;
import br.com.clinicafacil.dto.request.RequestDTOs.PacienteRequest;
import br.com.clinicafacil.repository.EspecialidadeRepository;
import br.com.clinicafacil.repository.PacienteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Teste de INTEGRAÇÃO para os endpoints REST do ClinicaFácil.
 *
 * Conforme orientado na disciplina:
 *   "@SpringBootTest
 *    public class ClienteServiceTest {
 *        @Autowired private ClienteService clienteService;
 *        @Test public void deveCriarCliente() { ... }
 *    }"
 *
 * @SpringBootTest: sobe o contexto completo do Spring (equivalente a rodar a app).
 * @AutoConfigureMockMvc: configura o MockMvc para simular requisições HTTP.
 * @ActiveProfiles("test"): usa o application-test.properties com H2 em memória,
 *   evitando dependência de um MySQL real durante os testes.
 *
 * O banco H2 é criado e destruído a cada execução dos testes (ddl-auto=create-drop).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Testes de Integração — ClinicaFácil API")
class IntegracaoApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EspecialidadeRepository especialidadeRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Limpa o banco H2 antes de cada teste para isolamento
        pacienteRepository.deleteAll();
        especialidadeRepository.deleteAll();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    // ─────────────────────────────────────────────────────────────
    // Teste 1 (Integração): Criar especialidade via POST e buscar via GET
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/especialidades → deve criar e retornar 201")
    void deveCriarEspecialidadeComSucesso() throws Exception {
        EspecialidadeRequest request = new EspecialidadeRequest("Cardiologia", "Coração");

        mockMvc.perform(post("/api/especialidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Cardiologia"))
                .andExpect(jsonPath("$.descricao").value("Coração"));
    }

    @Test
    @DisplayName("GET /api/especialidades → deve retornar lista vazia quando não há dados")
    void deveRetornarListaVazia() throws Exception {
        mockMvc.perform(get("/api/especialidades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("POST /api/especialidades → deve retornar 409 para nome duplicado")
    void deveRetornar409ParaNomeDuplicado() throws Exception {
        EspecialidadeRequest request = new EspecialidadeRequest("Ortopedia", "Ossos");

        // Primeira criação — deve ter sucesso
        mockMvc.perform(post("/api/especialidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Segunda criação com o mesmo nome — deve retornar 409 (Conflict)
        mockMvc.perform(post("/api/especialidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /api/especialidades/{id} → deve retornar 404 para ID inexistente")
    void deveRetornar404ParaEspecialidadeNaoEncontrada() throws Exception {
        mockMvc.perform(get("/api/especialidades/9999"))
                .andExpect(status().isNotFound());
    }

    // ─────────────────────────────────────────────────────────────
    // Teste 2 (Integração): Criar paciente e validar dados
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/pacientes → deve cadastrar paciente e retornar 201 (sem senha)")
    void deveCadastrarPacienteComSucesso() throws Exception {
        PacienteRequest request = new PacienteRequest(
                "Maria da Silva",
                "maria@email.com",
                "Senha@123",
                "123.456.789-00",
                LocalDate.of(1990, 5, 20),
                "(49) 99999-0000"
        );

        mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Maria da Silva"))
                .andExpect(jsonPath("$.email").value("maria@email.com"))
                .andExpect(jsonPath("$.cpf").value("123.456.789-00"))
                // Garante que a senha NÃO é exposta na resposta
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/pacientes → deve retornar 409 para CPF duplicado")
    void deveRetornar409ParaCpfDuplicado() throws Exception {
        PacienteRequest request = new PacienteRequest(
                "João Souza", "joao@email.com", "Senha@123",
                "999.888.777-66", LocalDate.of(1985, 1, 10), "(49) 98888-0000"
        );

        // Primeiro cadastro
        mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Segundo com mesmo CPF — deve retornar 409
        PacienteRequest duplicado = new PacienteRequest(
                "Carlos Lima", "carlos@email.com", "Senha@123",
                "999.888.777-66", LocalDate.of(1992, 3, 15), "(49) 97777-0000"
        );

        mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicado)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/pacientes → deve retornar 400 para dados inválidos (e-mail inválido)")
    void deveRetornar400ParaDadosInvalidos() throws Exception {
        PacienteRequest request = new PacienteRequest(
                "", // nome em branco — deve falhar na validação @NotBlank
                "email-invalido", // e-mail inválido — deve falhar em @Email
                "123", // senha muito curta — deve falhar em @Size(min=6)
                "000",
                null,
                null
        );

        mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos").exists());
    }

    // ─────────────────────────────────────────────────────────────
    // Teste 3 (Integração): CRUD completo de especialidades
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("CRUD completo de especialidade: criar → buscar → atualizar → deletar")
    void deveFazerCrudCompletoDeEspecialidade() throws Exception {
        // CREATE
        String corpo = mockMvc.perform(post("/api/especialidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Neurologia\",\"descricao\":\"Cérebro e sistema nervoso\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(corpo).get("id").asLong();

        // READ
        mockMvc.perform(get("/api/especialidades/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Neurologia"));

        // UPDATE
        mockMvc.perform(put("/api/especialidades/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Neurologia Clínica\",\"descricao\":\"Atualizado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Neurologia Clínica"));

        // DELETE
        mockMvc.perform(delete("/api/especialidades/" + id))
                .andExpect(status().isNoContent());

        // Confirma exclusão
        mockMvc.perform(get("/api/especialidades/" + id))
                .andExpect(status().isNotFound());
    }
}
