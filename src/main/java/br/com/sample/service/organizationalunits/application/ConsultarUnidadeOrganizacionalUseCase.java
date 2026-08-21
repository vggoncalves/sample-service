package br.com.sample.service.organizationalunits.application;

import br.com.sample.service.organizationalunits.domain.model.UnidadeOrganizacional;
import br.com.sample.service.organizationalunits.domain.repository.UnidadeOrganizacionalRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultarUnidadeOrganizacionalUseCase {
    private final UnidadeOrganizacionalRepository repository;

    public ConsultarUnidadeOrganizacionalUseCase(UnidadeOrganizacionalRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public UnidadeOrganizacional executar(UUID id) {
        return repository.buscarPorId(id).orElseThrow(() -> new UnidadeOrganizacionalNaoEncontradaException(id));
    }
}
