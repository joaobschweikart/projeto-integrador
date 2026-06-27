package br.com.clinicafacil.service;

import br.com.clinicafacil.dto.request.RequestDTOs.EspecialidadeRequest;
import br.com.clinicafacil.dto.response.ResponseDTOs.EspecialidadeResponse;
import br.com.clinicafacil.entity.Especialidade;
import br.com.clinicafacil.exception.GlobalExceptionHandler.RecursoNaoEncontradoException;
import br.com.clinicafacil.exception.GlobalExceptionHandler.RegraDeNegocioException;
import br.com.clinicafacil.repository.EspecialidadeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Teste UNITÁRIO para EspecialidadeService.
 *
 * Conforme orientado na disciplina:
 *   "Utilize @Test para definir métodos de teste no JUnit.
 *    Criação de Testes Unitários: utilize @Test para definir métodos."
 *
 * Utiliza Mockito para simular o repository, eliminando dependência
 * de banco de dados real — os testes rodam de forma isolada e rápida.
 *
 * @ExtendWith(MockitoExtension.class): inicializa os mocks automaticamente.
 * @Mock: cria um mock do repository (sem banco real).
 * @InjectMocks: injeta os mocks no service a ser testado.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EspecialidadeService — Testes Unitários")
class EspecialidadeServiceTest {

    @Mock
    private EspecialidadeRepository especialidadeRepository;

    @InjectMocks
    private EspecialidadeService especialidadeService;

    private Especialidade especialidadeExistente;

    @BeforeEach
    void setUp() {
        // Monta uma especialidade fictícia reutilizada nos testes
        especialidadeExistente = Especialidade.builder()
                .id(1L)
                .nome("Cardiologia")
                .descricao("Tratamento de doenças cardiovasculares")
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // Teste 1: Criar especialidade com sucesso
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve criar especialidade com sucesso quando nome não existe")
    void deveCriarEspecialidadeComSucesso() {
        // ── ARRANGE (preparação) ──────────────────────────────────
        EspecialidadeRequest request = new EspecialidadeRequest("Cardiologia", "Coração");

        // Mock: nome ainda não existe no banco
        when(especialidadeRepository.existsByNomeIgnoreCase("Cardiologia"))
                .thenReturn(false);

        // Mock: save() retorna a entidade com ID preenchido
        when(especialidadeRepository.save(any(Especialidade.class)))
                .thenReturn(especialidadeExistente);

        // ── ACT (execução) ────────────────────────────────────────
        EspecialidadeResponse response = especialidadeService.criar(request);

        // ── ASSERT (verificação) ──────────────────────────────────
        assertNotNull(response, "A resposta não deve ser nula");
        assertNotNull(response.id(), "O ID deve ser preenchido após o save");
        assertEquals("Cardiologia", response.nome(), "O nome deve ser igual ao informado");

        // Verifica que save() foi chamado exatamente uma vez
        verify(especialidadeRepository, times(1)).save(any(Especialidade.class));
    }

    // ─────────────────────────────────────────────────────────────
    // Teste 2: Rejeitar nome duplicado
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve lançar RegraDeNegocioException quando nome já existe")
    void deveLancarExcecaoQuandoNomeJaExiste() {
        // ── ARRANGE ───────────────────────────────────────────────
        EspecialidadeRequest request = new EspecialidadeRequest("Cardiologia", "Duplicado");

        // Mock: nome JÁ existe no banco
        when(especialidadeRepository.existsByNomeIgnoreCase("Cardiologia"))
                .thenReturn(true);

        // ── ACT + ASSERT ──────────────────────────────────────────
        RegraDeNegocioException ex = assertThrows(
                RegraDeNegocioException.class,
                () -> especialidadeService.criar(request),
                "Deve lançar RegraDeNegocioException para nome duplicado"
        );

        assertTrue(ex.getMessage().contains("Cardiologia"),
                "A mensagem deve mencionar o nome duplicado");

        // Verifica que save() NUNCA foi chamado
        verify(especialidadeRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────
    // Teste 3: Buscar por ID não encontrado
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException para ID inexistente")
    void deveLancarExcecaoQuandoIdNaoEncontrado() {
        // ── ARRANGE ───────────────────────────────────────────────
        when(especialidadeRepository.findById(99L))
                .thenReturn(Optional.empty());

        // ── ACT + ASSERT ──────────────────────────────────────────
        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> especialidadeService.buscarPorId(99L),
                "Deve lançar RecursoNaoEncontradoException para ID inexistente"
        );
    }

    // ─────────────────────────────────────────────────────────────
    // Teste 4: Listar todas retorna lista correta
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve retornar lista com todas as especialidades")
    void deveListarTodasEspecialidades() {
        // ── ARRANGE ───────────────────────────────────────────────
        Especialidade segunda = Especialidade.builder()
                .id(2L).nome("Ortopedia").descricao("Ossos e articulações").build();

        when(especialidadeRepository.findAll())
                .thenReturn(List.of(especialidadeExistente, segunda));

        // ── ACT ───────────────────────────────────────────────────
        List<EspecialidadeResponse> lista = especialidadeService.listarTodas();

        // ── ASSERT ────────────────────────────────────────────────
        assertNotNull(lista);
        assertEquals(2, lista.size(), "Deve retornar exatamente 2 especialidades");
        assertEquals("Cardiologia", lista.get(0).nome());
        assertEquals("Ortopedia", lista.get(1).nome());
    }

    // ─────────────────────────────────────────────────────────────
    // Teste 5: Excluir especialidade com sucesso
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve excluir especialidade quando ela existe")
    void deveExcluirEspecialidadeComSucesso() {
        // ── ARRANGE ───────────────────────────────────────────────
        when(especialidadeRepository.findById(1L))
                .thenReturn(Optional.of(especialidadeExistente));

        doNothing().when(especialidadeRepository).delete(especialidadeExistente);

        // ── ACT ───────────────────────────────────────────────────
        assertDoesNotThrow(() -> especialidadeService.excluir(1L));

        // ── ASSERT ────────────────────────────────────────────────
        verify(especialidadeRepository, times(1)).delete(especialidadeExistente);
    }
}
