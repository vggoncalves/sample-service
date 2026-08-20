package br.com.sample.service.organizationalunits.infrastructure.persistence;

import br.com.sample.service.organizationalunits.domain.model.UnidadeOrganizacional;
import br.com.sample.service.organizationalunits.domain.model.UnidadeOrganizacionalPersistida;
import br.com.sample.service.organizationalunits.domain.repository.UnidadeOrganizacionalRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class JpaUnidadeOrganizacionalRepositoryAdapter implements UnidadeOrganizacionalRepository {
    private final SpringDataUnidadeOrganizacionalRepository repository;

    JpaUnidadeOrganizacionalRepositoryAdapter(SpringDataUnidadeOrganizacionalRepository repository) {
        this.repository = repository;
    }

    @Override
    public UnidadeOrganizacional salvar(UnidadeOrganizacional unidade) {
        return paraDominio(repository.saveAndFlush(paraEntidade(unidade)));
    }

    @Override
    public Optional<UnidadeOrganizacional> buscarPorId(UUID id) {
        return repository.findById(id.toString()).map(this::paraDominio);
    }

    @Override
    public List<UnidadeOrganizacional> buscarFilhasDiretas(UUID unidadePaiId) {
        return repository.findByUnidadePaiId(unidadePaiId.toString()).stream().map(this::paraDominio).toList();
    }

    @Override
    public boolean existePorCodigo(String codigo) {
        return repository.existsByCodigo(codigo);
    }

    private UnidadeOrganizacionalJpaEntity paraEntidade(UnidadeOrganizacional unidade) {
        return UnidadeOrganizacionalJpaEntity.de(unidade.estadoPersistido());
    }

    private UnidadeOrganizacional paraDominio(UnidadeOrganizacionalJpaEntity entidade) {
        return UnidadeOrganizacional.restaurar(new UnidadeOrganizacionalPersistida(
                UUID.fromString(entidade.getId()), entidade.getCodigo(), entidade.getNome(), entidade.getSigla(),
                entidade.getDescricao(), entidade.getTipo(), paraUuid(entidade.getUnidadePaiId()),
                entidade.getEmailContato(), entidade.getTelefone(), entidade.isAtiva(),
                Instant.parse(entidade.getCriadoEm()), entidade.getCriadoPor(),
                Instant.parse(entidade.getAtualizadoEm()), entidade.getAtualizadoPor(), entidade.getVersao()));
    }

    private static UUID paraUuid(String id) {
        return id == null ? null : UUID.fromString(id);
    }
}
