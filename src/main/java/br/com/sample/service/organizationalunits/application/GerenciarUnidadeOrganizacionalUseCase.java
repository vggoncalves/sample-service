package br.com.sample.service.organizationalunits.application;

import br.com.sample.service.organizationalunits.domain.model.DadosBasicosUnidade;
import br.com.sample.service.organizationalunits.domain.model.UnidadeOrganizacional;
import br.com.sample.service.organizationalunits.domain.repository.UnidadeOrganizacionalRepository;
import br.com.sample.service.organizationalunits.domain.service.PoliticaHierarquiaUnidadeOrganizacional;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GerenciarUnidadeOrganizacionalUseCase {
    private static final String ATOR_SISTEMA = "sistema";
    private final UnidadeOrganizacionalRepository repository;
    private final Clock clock;

    public GerenciarUnidadeOrganizacionalUseCase(UnidadeOrganizacionalRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public UnidadeOrganizacional alterar(UUID id, long versao, DadosBasicosUnidade dados) {
        var unidade = buscarNaVersao(id, versao);
        var politica = new PoliticaHierarquiaUnidadeOrganizacional(repository);
        politica.alterarDadosBasicos(unidade, dados, clock.instant(), ATOR_SISTEMA);
        return repository.salvar(unidade);
    }

    @Transactional
    public UnidadeOrganizacional alterarSituacao(UUID id, long versao, boolean ativa) {
        var unidade = buscarNaVersao(id, versao);
        var politica = new PoliticaHierarquiaUnidadeOrganizacional(repository);
        if (ativa) {
            politica.reativar(unidade, clock.instant(), ATOR_SISTEMA);
        } else {
            politica.desativar(unidade, clock.instant(), ATOR_SISTEMA);
        }
        return repository.salvar(unidade);
    }

    private UnidadeOrganizacional buscarNaVersao(UUID id, long versao) {
        var unidade = repository.buscarPorId(id).orElseThrow(() -> new UnidadeOrganizacionalNaoEncontradaException(id));
        if (unidade.versao() != versao) {
            throw new ConflitoVersaoUnidadeException();
        }
        return unidade;
    }
}
