package br.com.sample.service.organizationalunits.domain.repository;

import br.com.sample.service.organizationalunits.domain.model.UnidadeOrganizacionalPersistida;
import java.util.Optional;
import java.util.UUID;

public interface UnidadeOrganizacionalRepository {

    UnidadeOrganizacionalPersistida salvar(UnidadeOrganizacionalPersistida unidade);

    Optional<UnidadeOrganizacionalPersistida> buscarPorId(UUID id);

    boolean existePorCodigo(String codigo);
}
