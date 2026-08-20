package br.com.sample.service.organizationalunits.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.sample.service.organizationalunits.domain.exception.DadoUnidadeInvalidoException;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class DadosBasicosUnidadeLimitesTests {
    private static final Instant AGORA = Instant.parse("2026-08-19T12:00:00Z");

    @Test
    void aceitaLimitesInclusivosAprovados() {
        var emailCom254Caracteres = "a".repeat(242) + "@example.com";
        var dados = new DadosBasicosUnidade(
                "N".repeat(150), "S".repeat(30), "D".repeat(500), TipoUnidade.OUTRA, null,
                emailCom254Caracteres, "+" + "1".repeat(15));

        var unidade = UnidadeOrganizacional.criar("A".repeat(50), dados, AGORA, "A".repeat(255));

        assertThat(unidade.codigo()).hasSize(50);
        assertThat(unidade.nome()).hasSize(150);
        assertThat(unidade.sigla()).hasSize(30);
        assertThat(unidade.descricao()).hasSize(500);
        assertThat(unidade.emailContato()).hasSize(254);
        assertThat(unidade.telefone()).hasSize(16);
        assertThat(unidade.criadoPor()).hasSize(255);
    }

    @Test
    void rejeitaValoresAcimaDosLimites() {
        assertCampo(() -> UnidadeOrganizacional.criar("A".repeat(51), dados("Nome"), AGORA, "ator"), "codigo");
        assertCampo(() -> dados("N".repeat(151)), "nome");
        assertCampo(() -> new DadosBasicosUnidade(
                "Nome", null, null, TipoUnidade.OUTRA, null, "a".repeat(243) + "@example.com", null),
                "emailContato");
        assertCampo(() -> new DadosBasicosUnidade(
                "Nome", null, null, TipoUnidade.OUTRA, null, null, "+" + "1".repeat(16)), "telefone");
        assertCampo(() -> UnidadeOrganizacional.criar("OK", dados("Nome"), AGORA, "A".repeat(256)), "criadoPor");
    }

    @Test
    void aceitaLimitesMinimos() {
        var unidade = UnidadeOrganizacional.criar("A1", dados("ABC"), AGORA, "a");
        assertThat(unidade.codigo()).isEqualTo("A1");
        assertThat(unidade.nome()).isEqualTo("ABC");
        assertThat(new DadosBasicosUnidade(
                "ABC", null, null, TipoUnidade.OUTRA, null, null, "+" + "1".repeat(8)).telefone())
                .hasSize(9);
    }

    private static DadosBasicosUnidade dados(String nome) {
        return new DadosBasicosUnidade(nome, null, null, TipoUnidade.OUTRA, null, null, null);
    }

    private static void assertCampo(Runnable operacao, String campo) {
        assertThatThrownBy(operacao::run)
                .isInstanceOf(DadoUnidadeInvalidoException.class)
                .extracting("codigoErro", "campo")
                .containsExactly("UNIDADE-0002", campo);
    }
}
