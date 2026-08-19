package br.com.sample.service.organizationalunits.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.sql.DataSource;

final class PersistenceSchemaAssertions {

    private static final String INSERT = """
            INSERT INTO unidade_organizacional
                (id, codigo, nome, tipo, unidade_pai_id, ativa,
                 criado_em, criado_por, atualizado_em, atualizado_por, versao)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private PersistenceSchemaAssertions() {
    }

    static void assertConstraintsAndIndexes(DataSource dataSource) throws SQLException {
        try (var connection = dataSource.getConnection()) {
            assertThat(indexNames(connection)).contains(
                    "ix_unidade_organizacional_pai",
                    "ix_unidade_organizacional_ativa_nome",
                    "ix_unidade_organizacional_tipo",
                    "ix_unidade_organizacional_nome");

            assertThatThrownBy(() -> insert(connection, UUID.randomUUID(), "TIPO_INVALIDO", "INVALIDO", null))
                    .isInstanceOf(SQLException.class);

            var selfId = UUID.randomUUID();
            assertThatThrownBy(() -> insert(connection, selfId, "AUTO_PAI", "INSTITUICAO", selfId))
                    .isInstanceOf(SQLException.class);
        }
    }

    private static Set<String> indexNames(Connection connection) throws SQLException {
        var indexes = new HashSet<String>();
        try (var result = connection.getMetaData().getIndexInfo(null, null, "unidade_organizacional", false, false)) {
            while (result.next()) {
                if (result.getString("INDEX_NAME") != null) {
                    indexes.add(result.getString("INDEX_NAME").toLowerCase());
                }
            }
        }
        return indexes;
    }

    private static void insert(Connection connection, UUID id, String codigo, String tipo, UUID parentId)
            throws SQLException {
        try (var statement = connection.prepareStatement(INSERT)) {
            statement.setString(1, id.toString());
            statement.setString(2, codigo);
            statement.setString(3, "Unidade de teste");
            statement.setString(4, tipo);
            statement.setString(5, parentId == null ? null : parentId.toString());
            statement.setBoolean(6, true);
            statement.setString(7, "2026-08-18T12:00:00Z");
            statement.setString(8, "teste");
            statement.setString(9, "2026-08-18T12:00:00Z");
            statement.setString(10, "teste");
            statement.setLong(11, 0);
            statement.executeUpdate();
        }
    }
}
