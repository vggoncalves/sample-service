package br.com.sample.service.organizationalunits.infrastructure.persistence;

import br.com.sample.service.organizationalunits.domain.model.UnidadeOrganizacional;
import br.com.sample.service.organizationalunits.domain.model.UnidadeOrganizacionalPersistida;
import br.com.sample.service.organizationalunits.domain.repository.FiltroUnidadeOrganizacional;
import br.com.sample.service.organizationalunits.domain.repository.PaginaUnidades;
import br.com.sample.service.organizationalunits.domain.repository.UnidadeOrganizacionalRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
public class JpaUnidadeOrganizacionalRepositoryAdapter implements UnidadeOrganizacionalRepository {
    private static final String CAMPO_UNIDADE_PAI_ID = "unidadePaiId";
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
    public PaginaUnidades buscar(FiltroUnidadeOrganizacional filtro) {
        var pagina = repository.findAll(especificacao(filtro), PageRequest.of(filtro.pagina(), filtro.tamanho(), ordenacao(filtro)));
        return new PaginaUnidades(pagina.getContent().stream().map(this::paraDominio).toList(), pagina.getNumber(),
                pagina.getSize(), pagina.getTotalElements(), pagina.getTotalPages());
    }

    @Override
    public List<UnidadeOrganizacional> buscarFilhasDiretas(UUID unidadePaiId) {
        return repository.findByUnidadePaiId(unidadePaiId.toString()).stream().map(this::paraDominio).toList();
    }

    @Override
    public boolean existePorCodigo(String codigo) {
        return repository.existsByCodigo(codigo);
    }

    private static Specification<UnidadeOrganizacionalJpaEntity> especificacao(FiltroUnidadeOrganizacional filtro) {
        return (root, query, builder) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            adicionarFiltrosSimples(filtro, root, builder, predicates);
            adicionarFiltroNome(filtro, root, builder, predicates);
            adicionarFiltroPai(filtro, root, builder, predicates);
            adicionarFiltroRaiz(filtro, root, builder, predicates);
            return builder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }

    private static void adicionarFiltrosSimples(FiltroUnidadeOrganizacional filtro,
            jakarta.persistence.criteria.Root<UnidadeOrganizacionalJpaEntity> root,
            jakarta.persistence.criteria.CriteriaBuilder builder,
            List<jakarta.persistence.criteria.Predicate> predicates) {
        adicionarIgual(predicates, builder, root, "codigo", filtro.codigo());
        adicionarIgual(predicates, builder, root, "sigla", filtro.sigla());
        adicionarIgual(predicates, builder, root, "tipo", filtro.tipo());
        adicionarIgual(predicates, builder, root, "ativa", filtro.ativa());
    }

    private static void adicionarFiltroNome(FiltroUnidadeOrganizacional filtro,
            jakarta.persistence.criteria.Root<UnidadeOrganizacionalJpaEntity> root,
            jakarta.persistence.criteria.CriteriaBuilder builder,
            List<jakarta.persistence.criteria.Predicate> predicates) {
        if (filtro.nome() != null) {
            predicates.add(builder.like(builder.lower(root.get("nome")),
                    "%" + filtro.nome().toLowerCase(java.util.Locale.ROOT) + "%"));
        }
    }

    private static void adicionarFiltroPai(FiltroUnidadeOrganizacional filtro,
            jakarta.persistence.criteria.Root<UnidadeOrganizacionalJpaEntity> root,
            jakarta.persistence.criteria.CriteriaBuilder builder,
            List<jakarta.persistence.criteria.Predicate> predicates) {
        if (filtro.unidadePaiId() != null) {
            predicates.add(builder.equal(root.get(CAMPO_UNIDADE_PAI_ID), filtro.unidadePaiId().toString()));
        }
    }

    private static void adicionarFiltroRaiz(FiltroUnidadeOrganizacional filtro,
            jakarta.persistence.criteria.Root<UnidadeOrganizacionalJpaEntity> root,
            jakarta.persistence.criteria.CriteriaBuilder builder,
            List<jakarta.persistence.criteria.Predicate> predicates) {
        if (filtro.raiz() != null) {
            boolean raiz = Boolean.TRUE.equals(filtro.raiz());
            if (raiz) {
                predicates.add(builder.isNull(root.get(CAMPO_UNIDADE_PAI_ID)));
                return;
            }
            predicates.add(builder.isNotNull(root.get(CAMPO_UNIDADE_PAI_ID)));
        }
    }

    private static void adicionarIgual(List<jakarta.persistence.criteria.Predicate> predicates,
            jakarta.persistence.criteria.CriteriaBuilder builder,
            jakarta.persistence.criteria.Root<UnidadeOrganizacionalJpaEntity> root, String campo, Object valor) {
        if (valor != null) {
            predicates.add(builder.equal(root.get(campo), valor));
        }
    }

    private static Sort ordenacao(FiltroUnidadeOrganizacional filtro) {
        return Sort.by(filtro.ordenacoes().stream().map(ordem -> new Sort.Order(
                ordem.descendente() ? Sort.Direction.DESC : Sort.Direction.ASC, ordem.campo())).toList());
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
