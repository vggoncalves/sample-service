package br.com.sample.service.organizationalunits.domain.repository;

import br.com.sample.service.organizationalunits.domain.model.UnidadeOrganizacional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UnidadeOrganizacionalRepository {
    UnidadeOrganizacional salvar(UnidadeOrganizacional unidade);
    Optional<UnidadeOrganizacional> buscarPorId(UUID id);
    PaginaUnidades buscar(FiltroUnidadeOrganizacional filtro);
    List<UnidadeOrganizacional> buscarFilhasDiretas(UUID unidadePaiId);
    boolean existePorCodigo(String codigo);
}
