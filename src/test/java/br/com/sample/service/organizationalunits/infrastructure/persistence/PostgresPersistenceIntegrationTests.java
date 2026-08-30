package br.com.sample.service.organizationalunits.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.sample.service.organizationalunits.domain.model.DadosBasicosUnidade;
import br.com.sample.service.organizationalunits.domain.model.TipoUnidade;
import br.com.sample.service.organizationalunits.domain.model.UnidadeOrganizacional;
import br.com.sample.service.organizationalunits.domain.repository.UnidadeOrganizacionalRepository;
import java.time.Instant;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@ActiveProfiles("local-postgres")
@Testcontainers
@SpringBootTest
class PostgresPersistenceIntegrationTests {
    private static final String SENHA_ADMIN_LOCAL = UUID.randomUUID().toString();
    private static final String SENHA_CONSULTA_LOCAL = UUID.randomUUID().toString();
    @Container
    @ServiceConnection
    @SuppressWarnings("resource")
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("sample_test")
            .withUsername("sample")
            .withPassword("sample");

    @Autowired
    private UnidadeOrganizacionalRepository repository;
    @Autowired
    private DataSource dataSource;

    @DynamicPropertySource
    static void securityProperties(DynamicPropertyRegistry registry) {
        registry.add("SAMPLE_LOCAL_ADMIN_PASSWORD", () -> SENHA_ADMIN_LOCAL);
        registry.add("SAMPLE_LOCAL_CONSULTA_PASSWORD", () -> SENHA_CONSULTA_LOCAL);
    }

    @Test
    void iniciaContextoAplicaMigrationEExecutaRoundTripDoAgregado() throws Exception {
        var instante = Instant.parse("2026-08-19T12:00:00Z");
        var raiz = UnidadeOrganizacional.criar("MATRIZ", dados("Unidade Matriz", null), instante, "teste");
        var raizSalva = repository.salvar(raiz);
        var filha = UnidadeOrganizacional.criar(
                "FILIAL-01", dados("Filial Norte", raizSalva.id()), instante, "teste");
        var filhaSalva = repository.salvar(filha);

        assertThat(repository.buscarPorId(raiz.id()).orElseThrow().estadoPersistido())
                .isEqualTo(raizSalva.estadoPersistido());
        assertThat(repository.buscarFilhasDiretas(raiz.id()))
                .extracting(unidade -> unidade.id()).containsExactly(filhaSalva.id());
        assertThat(repository.existePorCodigo("MATRIZ")).isTrue();
        try (var connection = dataSource.getConnection()) {
            try (var result = connection.getMetaData().getTables(null, null, "unidade_organizacional", null)) {
                assertThat(result.next()).isTrue();
            }
        }
        PersistenceSchemaAssertions.assertConstraintsAndIndexes(dataSource);
    }

    private static DadosBasicosUnidade dados(String nome, java.util.UUID paiId) {
        return new DadosBasicosUnidade(nome, null, null, TipoUnidade.INSTITUICAO, paiId, null, null);
    }
}
