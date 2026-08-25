package br.com.sample.service.organizationalunits.application;

import br.com.sample.service.organizationalunits.domain.model.DadosBasicosUnidade;
import br.com.sample.service.organizationalunits.domain.model.UnidadeOrganizacional;
import br.com.sample.service.organizationalunits.domain.repository.UnidadeOrganizacionalRepository;
import br.com.sample.service.organizationalunits.domain.service.PoliticaHierarquiaUnidadeOrganizacional;
import br.com.sample.service.security.PrincipalAtual;
import java.time.Clock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CriarUnidadeOrganizacionalUseCase {
    private final UnidadeOrganizacionalRepository repository;
    private final Clock clock;
    private final PrincipalAtual principalAtual;
    private final ApplicationEventPublisher eventPublisher;

    public CriarUnidadeOrganizacionalUseCase(UnidadeOrganizacionalRepository repository, Clock clock,
            PrincipalAtual principalAtual, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.clock = clock;
        this.principalAtual = principalAtual;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public UnidadeOrganizacional executar(String codigo, DadosBasicosUnidade dados) {
        var unidade = UnidadeOrganizacional.criar(codigo, dados, clock.instant(), principalAtual.identificador());
        if (repository.existePorCodigo(unidade.codigo())) {
            throw new CodigoUnidadeDuplicadoException();
        }
        new PoliticaHierarquiaUnidadeOrganizacional(repository).validarCriacao(unidade);
        var unidadeSalva = repository.salvar(unidade);
        eventPublisher.publishEvent(new EventoAdministrativoUnidade("CRIACAO", unidadeSalva.id(),
                principalAtual.identificador()));
        return unidadeSalva;
    }
}
