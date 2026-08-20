package br.com.sample.service.organizationalunits.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataUnidadeOrganizacionalRepository
        extends JpaRepository<UnidadeOrganizacionalJpaEntity, String> {
    boolean existsByCodigo(String codigo);
    List<UnidadeOrganizacionalJpaEntity> findByUnidadePaiId(String unidadePaiId);
}
