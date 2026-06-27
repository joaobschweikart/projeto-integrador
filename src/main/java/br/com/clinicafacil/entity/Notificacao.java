package br.com.clinicafacil.entity;

import br.com.clinicafacil.entity.enums.TipoNotificacaoEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Representa uma notificação enviada ao paciente (por e-mail).
 *
 * Conforme o Diagrama de Classes :
 *   Atributos: -id: Long, -tipo: TipoNotificacaoEnum,
 *              -mensagem: String, -enviadoEm: DateTime
 *   Método: +enviar(): void
 *
 * Conforme o modelo do banco :
 *   Tabela: notificacao (id, consulta_id, tipo, mensagem, enviado_em)
 *   Relação: N:1 com Consulta — uma consulta gera N notificações.
 *
 * Conforme a Proposta de Valor:
 *   "Envio de notificações de confirmação e lembrete de consulta (por e-mail)"
 *   é uma das principais funcionalidades da plataforma.
 *
 * Conforme o Diagrama de Sequência :
 *   Após salvar a consulta, o ConsultaService chama enviarNotificacao().
 */
@Entity
@Table(name = "notificacao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "consulta")
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Consulta à qual esta notificação está vinculada.
     * FK: consulta_id → tabela consulta.
     * Relação N:1 — várias notificações por consulta.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consulta_id", nullable = false)
    private Consulta consulta;

    /**
     * Tipo da notificação.
     * Conforme atributo "-tipo: TipoNotificacaoEnum" do Diagrama de Classes.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoNotificacaoEnum tipo;

    /**
     * Conteúdo da mensagem enviada ao paciente.
     * Conforme atributo "-mensagem: String".
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    /**
     * Data/hora em que a notificação foi enviada.
     * Conforme atributo "-enviadoEm: DateTime".
     */
    @Column(name = "enviado_em")
    private LocalDateTime enviadoEm;

    @PrePersist
    protected void prePersist() {
        this.enviadoEm = LocalDateTime.now();
    }

    /**
     * Simula o envio da notificação.
     * Em produção: integrar com JavaMailSender (Spring Mail) ou
     * WhatsApp Business API (sugestão de evolução, seção 4).
     * Método: +enviar(): void (Diagrama de Classes).
     */
    public void enviar() {
        this.enviadoEm = LocalDateTime.now();
        // TODO: Integrar com serviço de e-mail (JavaMailSender)
        System.out.println("[NOTIFICACAO] " + tipo + " enviada para consulta #" + consulta.getId());
    }
}
