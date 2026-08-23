package br.com.sample.service.organizationalunits.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.sample.service.organizationalunits.domain.model.DadosBasicosUnidade;
import br.com.sample.service.organizationalunits.domain.model.TipoUnidade;
import br.com.sample.service.organizationalunits.domain.model.UnidadeOrganizacional;
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

    @Autowired private UnidadeOrganizacionalRepository repository;
    @Autowired private DataSource dataSource;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + DATABASE);
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
                .extracting(unidade -> unidade.id())
                .containsExactly(filhaSalva.id());
        assertThat(repository.existePorCodigo("MATRIZ")).isTrue();
        try (var connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getTables(null, null, "unidade_organizacional", null).next()).isTrue();
            assertThat(connection.createStatement().executeQuery("PRAGMA foreign_keys").getInt(1)).isEqualTo(1);
        }
        PersistenceSchemaAssertions.assertConstraintsAndIndexes(dataSource);
    }

    private static DadosBasicosUnidade dados(String nome, UUID paiId) {
        return new DadosBasicosUnidade(nome, null, null, TipoUnidade.INSTITUICAO, paiId, null, null);
    }
}
