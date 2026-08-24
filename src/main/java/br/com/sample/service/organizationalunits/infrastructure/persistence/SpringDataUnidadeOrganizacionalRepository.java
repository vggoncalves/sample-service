package br.com.sample.service.organizationalunits.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface SpringDataUnidadeOrganizacionalRepository
        extends JpaRepository<UnidadeOrganizacionalJpaEntity, String>, JpaSpecificationExecutor<UnidadeOrganizacionalJpaEntity> {
    boolean existsByCodigo(String codigo);
    List<UnidadeOrganizacionalJpaEntity> findByUnidadePaiId(String unidadePaiId);
    List<UnidadeOrganizacionalJpaEntity> findByUnidadePaiIdIsNull();
}
