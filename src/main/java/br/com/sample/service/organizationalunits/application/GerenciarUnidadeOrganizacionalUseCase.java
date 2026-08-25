package br.com.sample.service.organizationalunits.application;

import br.com.sample.service.organizationalunits.domain.model.DadosBasicosUnidade;
import br.com.sample.service.organizationalunits.domain.model.UnidadeOrganizacional;
import br.com.sample.service.organizationalunits.domain.repository.UnidadeOrganizacionalRepository;
import br.com.sample.service.organizationalunits.domain.service.PoliticaHierarquiaUnidadeOrganizacional;
import br.com.sample.service.security.PrincipalAtual;
import java.time.Clock;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GerenciarUnidadeOrganizacionalUseCase {
    private final UnidadeOrganizacionalRepository repository;
    private final Clock clock;
    private final PrincipalAtual principalAtual;
    private final ApplicationEventPublisher eventPublisher;

    public GerenciarUnidadeOrganizacionalUseCase(UnidadeOrganizacionalRepository repository, Clock clock,
            PrincipalAtual principalAtual, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.clock = clock;
        this.principalAtual = principalAtual;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public UnidadeOrganizacional alterar(UUID id, long versao, DadosBasicosUnidade dados) {
        var unidade = buscarNaVersao(id, versao);
        var politica = new PoliticaHierarquiaUnidadeOrganizacional(repository);
        politica.alterarDadosBasicos(unidade, dados, clock.instant(), principalAtual.identificador());
        var unidadeSalva = repository.salvar(unidade);
        registrarEvento("ALTERACAO", unidadeSalva.id());
        return unidadeSalva;
    }

    @Transactional
    public UnidadeOrganizacional alterarSituacao(UUID id, long versao, boolean ativa) {
        var unidade = buscarNaVersao(id, versao);
        var politica = new PoliticaHierarquiaUnidadeOrganizacional(repository);
        if (ativa) {
            politica.reativar(unidade, clock.instant(), principalAtual.identificador());
        } else {
            politica.desativar(unidade, clock.instant(), principalAtual.identificador());
        }
        var unidadeSalva = repository.salvar(unidade);
        registrarEvento(ativa ? "REATIVACAO" : "DESATIVACAO", unidadeSalva.id());
        return unidadeSalva;
    }

    private UnidadeOrganizacional buscarNaVersao(UUID id, long versao) {
        var unidade = repository.buscarPorId(id).orElseThrow(() -> new UnidadeOrganizacionalNaoEncontradaException(id));
        if (unidade.versao() != versao) {
            throw new ConflitoVersaoUnidadeException();
        }
        return unidade;
    }

    private void registrarEvento(String acao, UUID unidadeId) {
        eventPublisher.publishEvent(new EventoAdministrativoUnidade(acao, unidadeId, principalAtual.identificador()));
    }
}
