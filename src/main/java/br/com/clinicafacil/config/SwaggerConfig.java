package br.com.clinicafacil.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração da documentação Swagger/OpenAPI para o ClinicaFácil.
 *
 * Conforme orientado na disciplina:
 *   "Documentação da API com Swagger:
 *    @Configuration
 *    public class SwaggerConfig {
 *        @Bean
 *        public OpenAPI customOpenAPI() {
 *            return new OpenAPI()
 *                .info(new Info()
 *                    .title("API do Projeto")
 *                    .version("1.0")
 *                    .description("Documentação da API com Swagger"));
 *        }
 *    }"
 *
 * Após subir a aplicação, acesse:
 *   http://localhost:8080/swagger-ui.html
 *
 * Conforme também orientado na disciplina:
 *   "Utilize Springdoc OpenAPI para gerar documentação automática."
 *   Springdoc: https://springdoc.org/
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ClinicaFácil — API REST")
                        .version("1.0.0")
                        .description("""
                                ## Sistema de Agendamento para Clínicas Médicas
                                
                                Projeto Integrador — UNOESC — Curso de Análise e Desenvolvimento de Sistemas
                                
                                **Aluno:** João Vitor Bernardon Schweikart
                                
                                ### Casos de uso implementados:
                                - **UC02** — Agendar Consulta (com verificação de conflito)
                                - **UC03** — Cancelar Consulta
                                - **UC04** — Remarcar Consulta
                                - **UC05** — Visualizar Agenda do Médico
                                - **UC06** — Receber Notificação (gerada automaticamente)
                                - **UC07** — Cadastrar Paciente
                                - **UC08** — Cadastrar Médico
                                - **UC09** — Cadastrar Especialidade
                                - **UC10** — Gerar Relatório Gerencial
                                - **UC11** — Registrar Observação
                                
                                ### Arquitetura:
                                MVC com Spring Boot, JPA/Hibernate e MySQL.
                                """));
    }
}
