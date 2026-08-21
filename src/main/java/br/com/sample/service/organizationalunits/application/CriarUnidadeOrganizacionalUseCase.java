package br.com.sample.service.organizationalunits.application;

import br.com.sample.service.organizationalunits.domain.model.DadosBasicosUnidade;
import br.com.sample.service.organizationalunits.domain.model.UnidadeOrganizacional;
import br.com.sample.service.organizationalunits.domain.repository.UnidadeOrganizacionalRepository;
import br.com.sample.service.organizationalunits.domain.service.PoliticaHierarquiaUnidadeOrganizacional;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CriarUnidadeOrganizacionalUseCase {
    private static final String ATOR_SISTEMA = "sistema";
    private final UnidadeOrganizacionalRepository repository;
    private final Clock clock;

    public CriarUnidadeOrganizacionalUseCase(UnidadeOrganizacionalRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public UnidadeOrganizacional executar(String codigo, DadosBasicosUnidade dados) {
        var unidade = UnidadeOrganizacional.criar(codigo, dados, clock.instant(), ATOR_SISTEMA);
        if (repository.existePorCodigo(unidade.codigo())) {
            throw new CodigoUnidadeDuplicadoException();
        }
        new PoliticaHierarquiaUnidadeOrganizacional(repository).validarCriacao(unidade);
        return repository.salvar(unidade);
    }
}
