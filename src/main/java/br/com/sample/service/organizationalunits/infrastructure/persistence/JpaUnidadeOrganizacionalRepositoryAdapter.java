package br.com.sample.service.organizationalunits.infrastructure.persistence;

import br.com.sample.service.organizationalunits.domain.model.UnidadeOrganizacionalPersistida;
import br.com.sample.service.organizationalunits.domain.repository.UnidadeOrganizacionalRepository;
import java.time.Instant;
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
    public UnidadeOrganizacionalPersistida salvar(UnidadeOrganizacionalPersistida unidade) {
        return paraDominio(repository.saveAndFlush(paraEntidade(unidade)));
    }

    @Override
    public Optional<UnidadeOrganizacionalPersistida> buscarPorId(UUID id) {
        return repository.findById(id.toString()).map(this::paraDominio);
    }

    @Override
    public boolean existePorCodigo(String codigo) {
        return repository.existsByCodigo(codigo);
    }

    private UnidadeOrganizacionalJpaEntity paraEntidade(UnidadeOrganizacionalPersistida unidade) {
        return UnidadeOrganizacionalJpaEntity.de(unidade);
    }

    private UnidadeOrganizacionalPersistida paraDominio(UnidadeOrganizacionalJpaEntity entidade) {
        return new UnidadeOrganizacionalPersistida(
                UUID.fromString(entidade.getId()), entidade.getCodigo(), entidade.getNome(), entidade.getSigla(),
                entidade.getDescricao(), entidade.getTipo(), paraUuid(entidade.getUnidadePaiId()),
                entidade.getEmailContato(), entidade.getTelefone(), entidade.isAtiva(),
                Instant.parse(entidade.getCriadoEm()), entidade.getCriadoPor(),
                Instant.parse(entidade.getAtualizadoEm()), entidade.getAtualizadoPor(), entidade.getVersao());
    }

    private static UUID paraUuid(String id) {
        return id == null ? null : UUID.fromString(id);
    }
}
