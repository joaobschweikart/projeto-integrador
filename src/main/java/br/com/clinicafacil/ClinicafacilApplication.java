package br.com.clinicafacil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da aplicação ClinicaFácil.
 *
 * @SpringBootApplication combina:
 *   - @Configuration: define esta classe como fonte de beans Spring
 *   - @EnableAutoConfiguration: habilita autoconfiguration do Spring Boot
 *   - @ComponentScan: varre os pacotes em busca de @Component, @Service,
 *     @Repository e @Controller
 *
 * Conforme orientado na disciplina:
 *   "Utilize o Spring Initializr para configurar um projeto com as
 *    dependências Spring Web, Spring Data JPA e MySQL Driver.
 *    Organize os pacotes do projeto seguindo a arquitetura MVC."
 *
 * Para executar:
 *   mvn spring-boot:run
 *   ou
 *   java -jar target/clinicafacil-1.0.0.jar
 *
 * Swagger disponível em: http://localhost:8080/swagger-ui.html
 */
@SpringBootApplication
public class ClinicafacilApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClinicafacilApplication.class, args);
    }
}
