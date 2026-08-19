package br.com.sample.service.organizationalunits.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataUnidadeOrganizacionalRepository
        extends JpaRepository<UnidadeOrganizacionalJpaEntity, String> {

    boolean existsByCodigo(String codigo);
}
