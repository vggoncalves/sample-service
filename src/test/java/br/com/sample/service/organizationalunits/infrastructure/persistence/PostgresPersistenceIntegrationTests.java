package br.com.sample.service.organizationalunits.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.sample.service.organizationalunits.domain.model.TipoUnidade;
import br.com.sample.service.organizationalunits.domain.model.UnidadeOrganizacionalPersistida;
import br.com.sample.service.organizationalunits.domain.repository.UnidadeOrganizacionalRepository;
import java.time.Instant;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@ActiveProfiles("local-postgres")
@Testcontainers
@SpringBootTest
class PostgresPersistenceIntegrationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("sample_test")
            .withUsername("sample")
            .withPassword("sample");

    @Autowired
    private UnidadeOrganizacionalRepository repository;

    @Autowired
    private DataSource dataSource;

    @Test
    void iniciaContextoAplicaMigrationEExecutaAdapter() throws Exception {
        var id = UUID.randomUUID();
        var instante = Instant.parse("2026-08-18T12:00:00Z");
        var unidade = new UnidadeOrganizacionalPersistida(
                id, "MATRIZ", "Unidade Matriz", "MTZ", null, TipoUnidade.INSTITUICAO, null,
                null, null, true, instante, "teste", instante, "teste", 0);

        var salva = repository.salvar(unidade);

        assertThat(repository.buscarPorId(id)).contains(salva);
        assertThat(repository.existePorCodigo("MATRIZ")).isTrue();
        try (var connection = dataSource.getConnection();
                var result = connection.getMetaData().getTables(null, null, "unidade_organizacional", null)) {
            assertThat(result.next()).isTrue();
        }
        PersistenceSchemaAssertions.assertConstraintsAndIndexes(dataSource);
    }
}
