package br.com.clinicafacil.repository;

import br.com.clinicafacil.entity.Notificacao;
import br.com.clinicafacil.entity.enums.TipoNotificacaoEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório JPA para Notificacao.
 *
 * Conforme o modelo do banco :
 *   notificacao.consulta_id FK → consulta.id (relação 1:N).
 */
@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    /** Lista todas as notificações de uma consulta. */
    List<Notificacao> findByConsultaId(Long consultaId);

    /** Lista notificações por tipo em todas as consultas. */
    List<Notificacao> findByTipo(TipoNotificacaoEnum tipo);
}
