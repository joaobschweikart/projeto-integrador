package br.com.clinicafacil.controller.web;

import br.com.clinicafacil.dto.request.ConsultaRequestDTOs.*;
import br.com.clinicafacil.dto.request.RequestDTOs.*;
import br.com.clinicafacil.dto.response.ResponseDTOs.*;
import br.com.clinicafacil.exception.GlobalExceptionHandler.*;
import br.com.clinicafacil.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller MVC para a interface gráfica do ClinicaFácil.
 *
 * Diferente dos @RestController (que retornam JSON para o Swagger/API),
 * este controller usa @Controller (sem o "Rest") e retorna o nome
 * de um template Thymeleaf que é renderizado como HTML no navegador.
 *
 * Reutiliza os mesmos Services da API REST — nenhuma lógica duplicada.
 *
 * Rotas disponíveis:
 *   GET  /                          → Dashboard
 *   GET  /web/especialidades        → Lista especialidades
 *   GET  /web/especialidades/nova   → Formulário nova especialidade
 *   POST /web/especialidades/salvar → Salva especialidade
 *   ... (mesma estrutura para pacientes, médicos e consultas)
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebController {

    private final EspecialidadeService especialidadeService;
    private final PacienteService      pacienteService;
    private final MedicoService        medicoService;
    private final ConsultaService      consultaService;

    // ══════════════════════════════════════════════════════════════
    // DASHBOARD
    // ══════════════════════════════════════════════════════════════

    @GetMapping("/")
    public String dashboard(Model model) {
        try {
            RelatorioResponse relatorio = consultaService.gerarRelatorio();
            List<ConsultaResponse> ultimas = consultaService.listarTodas()
                    .stream().limit(5).toList();
            long totalPacientes = pacienteService.listarTodos().size();

            model.addAttribute("relatorio", relatorio);
            model.addAttribute("ultimasConsultas", ultimas);
            model.addAttribute("totalPacientes", totalPacientes);
        } catch (Exception e) {
            // Dashboard nunca deve quebrar — mostra zerado se banco vazio
            model.addAttribute("relatorio", new RelatorioResponse(0,0,0,0,0,0,0.0));
            model.addAttribute("ultimasConsultas", List.of());
            model.addAttribute("totalPacientes", 0);
        }
        return "dashboard";
    }

    // ══════════════════════════════════════════════════════════════
    // ESPECIALIDADES
    // ══════════════════════════════════════════════════════════════

    @GetMapping("/web/especialidades")
    public String listarEspecialidades(Model model,
            @RequestParam(required = false) String sucesso,
            @RequestParam(required = false) String erro) {
        model.addAttribute("especialidades", especialidadeService.listarTodas());
        model.addAttribute("sucesso", sucesso);
        model.addAttribute("erro", erro);
        return "especialidades/lista";
    }

    @GetMapping("/web/especialidades/nova")
    public String novaEspecialidade(Model model) {
        // Objeto vazio para o th:value funcionar no formulário
        model.addAttribute("especialidade", new EspecialidadeResponse(null, "", ""));
        return "especialidades/form";
    }

    @GetMapping("/web/especialidades/editar/{id}")
    public String editarEspecialidade(@PathVariable Long id, Model model) {
        model.addAttribute("especialidade", especialidadeService.buscarPorId(id));
        return "especialidades/form";
    }

    @PostMapping("/web/especialidades/salvar")
    public String salvarEspecialidade(
            @RequestParam String nome,
            @RequestParam(required = false) String descricao,
            RedirectAttributes ra) {
        try {
            especialidadeService.criar(new EspecialidadeRequest(nome, descricao));
            ra.addFlashAttribute("sucesso", "Especialidade '" + nome + "' cadastrada com sucesso!");
        } catch (RegraDeNegocioException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/web/especialidades";
    }

    @PostMapping("/web/especialidades/salvar/{id}")
    public String atualizarEspecialidade(
            @PathVariable Long id,
            @RequestParam String nome,
            @RequestParam(required = false) String descricao,
            RedirectAttributes ra) {
        try {
            especialidadeService.atualizar(id, new EspecialidadeRequest(nome, descricao));
            ra.addFlashAttribute("sucesso", "Especialidade atualizada com sucesso!");
        } catch (RegraDeNegocioException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/web/especialidades";
    }

    @PostMapping("/web/especialidades/excluir/{id}")
    public String excluirEspecialidade(@PathVariable Long id, RedirectAttributes ra) {
        try {
            especialidadeService.excluir(id);
            ra.addFlashAttribute("sucesso", "Especialidade removida com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Não foi possível remover: " + e.getMessage());
        }
        return "redirect:/web/especialidades";
    }

    // ══════════════════════════════════════════════════════════════
    // PACIENTES
    // ══════════════════════════════════════════════════════════════

    @GetMapping("/web/pacientes")
    public String listarPacientes(Model model,
            @RequestParam(required = false) String nome) {
        var lista = (nome != null && !nome.isBlank())
                ? pacienteService.buscarPorNome(nome)
                : pacienteService.listarTodos();
        model.addAttribute("pacientes", lista);
        model.addAttribute("buscaNome", nome);
        return "pacientes/lista";
    }

    @GetMapping("/web/pacientes/novo")
    public String novoPaciente(Model model) {
        model.addAttribute("paciente", new PacienteResponse(null,"","","",null,"",null,null));
        return "pacientes/form";
    }

    @GetMapping("/web/pacientes/editar/{id}")
    public String editarPaciente(@PathVariable Long id, Model model) {
        model.addAttribute("paciente", pacienteService.buscarPorId(id));
        return "pacientes/form";
    }

    @PostMapping("/web/pacientes/salvar")
    public String salvarPaciente(
            @RequestParam String nome, @RequestParam String email,
            @RequestParam String senha, @RequestParam String cpf,
            @RequestParam(required = false) String dataNascimento,
            @RequestParam(required = false) String telefone,
            RedirectAttributes ra) {
        try {
            var dt = (dataNascimento != null && !dataNascimento.isBlank())
                    ? java.time.LocalDate.parse(dataNascimento) : null;
            pacienteService.criar(new PacienteRequest(nome, email, senha, cpf, dt, telefone));
            ra.addFlashAttribute("sucesso", "Paciente '" + nome + "' cadastrado com sucesso!");
        } catch (RegraDeNegocioException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/web/pacientes";
    }

    @PostMapping("/web/pacientes/salvar/{id}")
    public String atualizarPaciente(
            @PathVariable Long id,
            @RequestParam String nome, @RequestParam String email,
            @RequestParam(required = false) String senha,
            @RequestParam String cpf,
            @RequestParam(required = false) String dataNascimento,
            @RequestParam(required = false) String telefone,
            RedirectAttributes ra) {
        try {
            var dt = (dataNascimento != null && !dataNascimento.isBlank())
                    ? java.time.LocalDate.parse(dataNascimento) : null;
            String s = (senha == null || senha.isBlank()) ? "MANTER" : senha;
            pacienteService.atualizar(id, new PacienteRequest(nome, email, s, cpf, dt, telefone));
            ra.addFlashAttribute("sucesso", "Paciente atualizado com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/web/pacientes";
    }

    @PostMapping("/web/pacientes/excluir/{id}")
    public String excluirPaciente(@PathVariable Long id, RedirectAttributes ra) {
        try {
            pacienteService.excluir(id);
            ra.addFlashAttribute("sucesso", "Paciente removido com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Não foi possível remover: " + e.getMessage());
        }
        return "redirect:/web/pacientes";
    }

    // ══════════════════════════════════════════════════════════════
    // MÉDICOS
    // ══════════════════════════════════════════════════════════════

    @GetMapping("/web/medicos")
    public String listarMedicos(Model model) {
        model.addAttribute("medicos", medicoService.listarTodos());
        return "medicos/lista";
    }

    @GetMapping("/web/medicos/novo")
    public String novoMedico(Model model) {
        model.addAttribute("medico", new MedicoResponse(null,"","","",null,null));
        model.addAttribute("especialidades", especialidadeService.listarTodas());
        return "medicos/form";
    }

    @GetMapping("/web/medicos/editar/{id}")
    public String editarMedico(@PathVariable Long id, Model model) {
        model.addAttribute("medico", medicoService.buscarPorId(id));
        model.addAttribute("especialidades", especialidadeService.listarTodas());
        return "medicos/form";
    }

    @PostMapping("/web/medicos/salvar")
    public String salvarMedico(
            @RequestParam String nome, @RequestParam String email,
            @RequestParam String senha, @RequestParam String crm,
            @RequestParam Long especialidadeId,
            RedirectAttributes ra) {
        try {
            medicoService.criar(new MedicoRequest(nome, email, senha, crm, especialidadeId));
            ra.addFlashAttribute("sucesso", "Médico '" + nome + "' cadastrado com sucesso!");
        } catch (RegraDeNegocioException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/web/medicos";
    }

    @PostMapping("/web/medicos/salvar/{id}")
    public String atualizarMedico(
            @PathVariable Long id,
            @RequestParam String nome, @RequestParam String email,
            @RequestParam(required = false) String senha,
            @RequestParam String crm, @RequestParam Long especialidadeId,
            RedirectAttributes ra) {
        try {
            String s = (senha == null || senha.isBlank()) ? "MANTER" : senha;
            medicoService.atualizar(id, new MedicoRequest(nome, email, s, crm, especialidadeId));
            ra.addFlashAttribute("sucesso", "Médico atualizado com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/web/medicos";
    }

    @PostMapping("/web/medicos/excluir/{id}")
    public String excluirMedico(@PathVariable Long id, RedirectAttributes ra) {
        try {
            medicoService.excluir(id);
            ra.addFlashAttribute("sucesso", "Médico removido com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Não foi possível remover: " + e.getMessage());
        }
        return "redirect:/web/medicos";
    }

    // ══════════════════════════════════════════════════════════════
    // CONSULTAS
    // ══════════════════════════════════════════════════════════════

    @GetMapping("/web/consultas")
    public String listarConsultas(Model model) {
        model.addAttribute("consultas", consultaService.listarTodas());
        return "consultas/lista";
    }

    @GetMapping("/web/consultas/nova")
    public String novaConsulta(Model model) {
        model.addAttribute("pacientes", pacienteService.listarTodos());
        model.addAttribute("medicos", medicoService.listarTodos());
        model.addAttribute("remarcando", false);
        return "consultas/form";
    }

    @PostMapping("/web/consultas/salvar")
    public String salvarConsulta(
            @RequestParam Long pacienteId,
            @RequestParam Long medicoId,
            @RequestParam String dataHora,
            RedirectAttributes ra) {
        try {
            LocalDateTime dt = LocalDateTime.parse(dataHora);
            consultaService.agendar(new AgendarConsultaRequest(pacienteId, medicoId, dt));
            ra.addFlashAttribute("sucesso", "Consulta agendada com sucesso!");
        } catch (RegraDeNegocioException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/web/consultas/nova";
        }
        return "redirect:/web/consultas";
    }

    @PostMapping("/web/consultas/confirmar/{id}")
    public String confirmarConsulta(@PathVariable Long id, RedirectAttributes ra) {
        try {
            consultaService.confirmar(id);
            ra.addFlashAttribute("sucesso", "Consulta confirmada!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/web/consultas";
    }

    @PostMapping("/web/consultas/cancelar/{id}")
    public String cancelarConsulta(@PathVariable Long id, RedirectAttributes ra) {
        try {
            consultaService.cancelar(id);
            ra.addFlashAttribute("sucesso", "Consulta cancelada.");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/web/consultas";
    }

    @GetMapping("/web/consultas/remarcar/{id}")
    public String paginaRemarcar(@PathVariable Long id, Model model) {
        model.addAttribute("remarcando", true);
        model.addAttribute("consultaId", id);
        return "consultas/form";
    }

    @PostMapping("/web/consultas/remarcar/salvar/{id}")
    public String salvarRemarcacao(
            @PathVariable Long id,
            @RequestParam String novaDataHora,
            RedirectAttributes ra) {
        try {
            LocalDateTime dt = LocalDateTime.parse(novaDataHora);
            consultaService.remarcar(id, new RemarcarConsultaRequest(dt));
            ra.addFlashAttribute("sucesso", "Consulta remarcada com sucesso!");
        } catch (RegraDeNegocioException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/web/consultas";
    }
}
