package br.com.sample.service.organizationalunits.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class RegistroEventoAdministrativoUnidade {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegistroEventoAdministrativoUnidade.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void registrar(EventoAdministrativoUnidade evento) {
        LOGGER.info("evento=unidade-organizacional acao={} unidadeId={} responsavel={}", evento.acao(),
                evento.unidadeId(), evento.responsavel());
    }
}
