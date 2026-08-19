package br.com.sample.service.organizationalunits.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.sample.service.organizationalunits.domain.model.TipoUnidade;
import br.com.sample.service.organizationalunits.domain.model.UnidadeOrganizacionalPersistida;
import br.com.sample.service.organizationalunits.domain.repository.UnidadeOrganizacionalRepository;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@ActiveProfiles("local-sqlite")
@SpringBootTest
class SqlitePersistenceIntegrationTests {

    private static final Path DATABASE = Path.of("target", "sqlite-test-" + UUID.randomUUID() + ".db");

    @Autowired
    private UnidadeOrganizacionalRepository repository;

    @Autowired
    private DataSource dataSource;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + DATABASE);
    }

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
        try (var connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getTables(null, null, "unidade_organizacional", null).next()).isTrue();
            assertThat(connection.createStatement().executeQuery("PRAGMA foreign_keys").getInt(1)).isEqualTo(1);
        }
        PersistenceSchemaAssertions.assertConstraintsAndIndexes(dataSource);
    }
}
