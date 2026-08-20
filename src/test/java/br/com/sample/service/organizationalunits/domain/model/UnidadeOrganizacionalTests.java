package br.com.sample.service.organizationalunits.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.sample.service.organizationalunits.domain.exception.DadoUnidadeInvalidoException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UnidadeOrganizacionalTests {
    private static final Instant CRIACAO = Instant.parse("2026-08-19T10:00:00Z");

    @Test
    void criaRaizAtivaNormalizandoDadosEAuditoria() {
        var dados = new DadosBasicosUnidade(
                "  Unidade Central  ", "  uc  ", "  Descrição  ", TipoUnidade.INSTITUICAO, null,
                "  CONTATO@EXEMPLO.COM  ", " +55 (61) 99999-0000 ");

        var unidade = UnidadeOrganizacional.criar("  matriz-01  ", dados, CRIACAO, "  ator-1  ");

        assertThat(unidade.id()).isNotNull();
        assertThat(unidade.codigo()).isEqualTo("MATRIZ-01");
        assertThat(unidade.nome()).isEqualTo("Unidade Central");
        assertThat(unidade.sigla()).isEqualTo("UC");
        assertThat(unidade.descricao()).isEqualTo("Descrição");
        assertThat(unidade.emailContato()).isEqualTo("contato@exemplo.com");
        assertThat(unidade.telefone()).isEqualTo("+5561999990000");
        assertThat(unidade.tipo()).isEqualTo(TipoUnidade.INSTITUICAO);
        assertThat(unidade.unidadePaiId()).isNull();
        assertThat(unidade.ativa()).isTrue();
        assertThat(unidade.criadoEm()).isEqualTo(CRIACAO);
        assertThat(unidade.atualizadoEm()).isEqualTo(CRIACAO);
        assertThat(unidade.criadoPor()).isEqualTo("ator-1");
        assertThat(unidade.atualizadoPor()).isEqualTo("ator-1");
        assertThat(unidade.versao()).isZero();
    }

    @Test
    void converteCamposOpcionaisVaziosEmNulo() {
        var dados = new DadosBasicosUnidade(
                "Unidade", " ", "\t", TipoUnidade.OUTRA, null, " ", " ");

        assertThat(dados.sigla()).isNull();
        assertThat(dados.descricao()).isNull();
        assertThat(dados.emailContato()).isNull();
        assertThat(dados.telefone()).isNull();
    }

    @Test
    void restauraEstadoSemPerderIdentidadeAuditoriaSituacaoOuVersao() {
        var id = UUID.randomUUID();
        var paiId = UUID.randomUUID();
        var atualizado = CRIACAO.plusSeconds(60);
        var estado = new UnidadeOrganizacionalPersistida(
                id, " filial.01 ", " Filial Norte ", " fn ", null, TipoUnidade.FILIAL, paiId,
                null, "+5561999990000", false, CRIACAO, "criador", atualizado, "editor", 7);

        var unidade = UnidadeOrganizacional.restaurar(estado);

        assertThat(unidade.estadoPersistido()).isEqualTo(new UnidadeOrganizacionalPersistida(
                id, "FILIAL.01", "Filial Norte", "FN", null, TipoUnidade.FILIAL, paiId,
                null, "+5561999990000", false, CRIACAO, "criador", atualizado, "editor", 7));
    }

    @Test
    void alteraDadosSemPermitirAlteracaoDoCodigoOuDaAuditoriaDeCriacao() {
        var unidade = UnidadeOrganizacional.criar("COD-01", dados(null), CRIACAO, "criador");
        var instante = CRIACAO.plusSeconds(30);
        var paiId = UUID.randomUUID();

        unidade.alterarDadosBasicos(new DadosBasicosUnidade(
                "Novo nome", "nn", "Nova descrição", TipoUnidade.DIRETORIA, paiId,
                "novo@example.com", "+5511999999999"), instante, "editor");

        assertThat(unidade.codigo()).isEqualTo("COD-01");
        assertThat(unidade.nome()).isEqualTo("Novo nome");
        assertThat(unidade.unidadePaiId()).isEqualTo(paiId);
        assertThat(unidade.criadoEm()).isEqualTo(CRIACAO);
        assertThat(unidade.criadoPor()).isEqualTo("criador");
        assertThat(unidade.atualizadoEm()).isEqualTo(instante);
        assertThat(unidade.atualizadoPor()).isEqualTo("editor");
        assertThat(unidade.versao()).isZero();
    }

    @Test
    void rejeitaCamposInvalidosComCodigoECampoPadronizados() {
        assertInvalido(() -> UnidadeOrganizacional.criar(" x ", dados(null), CRIACAO, "ator"), "codigo");
        assertInvalido(() -> new DadosBasicosUnidade("ab", null, null, TipoUnidade.OUTRA, null, null, null), "nome");
        assertInvalido(() -> new DadosBasicosUnidade("Nome", "x".repeat(31), null, TipoUnidade.OUTRA, null, null, null), "sigla");
        assertInvalido(() -> new DadosBasicosUnidade("Nome", null, "x".repeat(501), TipoUnidade.OUTRA, null, null, null), "descricao");
        assertInvalido(() -> new DadosBasicosUnidade("Nome", null, null, null, null, null, null), "tipo");
        assertInvalido(() -> new DadosBasicosUnidade("Nome", null, null, TipoUnidade.OUTRA, null, "invalido", null), "emailContato");
        assertInvalido(() -> new DadosBasicosUnidade("Nome", null, null, TipoUnidade.OUTRA, null, null, "6199999"), "telefone");
        assertInvalido(() -> UnidadeOrganizacional.criar("OK", dados(null), null, "ator"), "criadoEm");
        assertInvalido(() -> UnidadeOrganizacional.criar("OK", dados(null), CRIACAO, " "), "criadoPor");
    }

    @Test
    void rejeitaEstadoPersistidoInconsistente() {
        var valido = estado(UUID.randomUUID(), null, true, 0, CRIACAO, CRIACAO);
        assertInvalido(() -> UnidadeOrganizacional.restaurar(null), "estado");
        assertInvalido(() -> UnidadeOrganizacional.restaurar(new UnidadeOrganizacionalPersistida(
                null, valido.codigo(), valido.nome(), valido.sigla(), valido.descricao(), valido.tipo(), null,
                valido.emailContato(), valido.telefone(), true, CRIACAO, "ator", CRIACAO, "ator", 0)), "id");
        assertInvalido(() -> UnidadeOrganizacional.restaurar(estado(
                UUID.randomUUID(), null, true, -1, CRIACAO, CRIACAO)), "versao");
        assertInvalido(() -> UnidadeOrganizacional.restaurar(estado(
                UUID.randomUUID(), null, true, 0, CRIACAO, CRIACAO.minusSeconds(1))), "atualizadoEm");
    }

    @Test
    void ativacaoEDesativacaoSaoIdempotentesEPreservamVersao() {
        var unidade = UnidadeOrganizacional.criar("OK", dados(null), CRIACAO, "ator");
        var desativacao = CRIACAO.plusSeconds(1);
        unidade.desativar(desativacao, "editor");
        unidade.desativar(CRIACAO.plusSeconds(2), "ignorado");
        assertThat(unidade.ativa()).isFalse();
        assertThat(unidade.atualizadoEm()).isEqualTo(desativacao);

        var reativacao = CRIACAO.plusSeconds(3);
        unidade.reativar(reativacao, "reativador");
        unidade.reativar(CRIACAO.plusSeconds(4), "ignorado");
        assertThat(unidade.ativa()).isTrue();
        assertThat(unidade.atualizadoEm()).isEqualTo(reativacao);
        assertThat(unidade.versao()).isZero();
    }

    private static DadosBasicosUnidade dados(UUID paiId) {
        return new DadosBasicosUnidade("Unidade válida", null, null, TipoUnidade.OUTRA, paiId, null, null);
    }

    private static UnidadeOrganizacionalPersistida estado(
            UUID id, UUID paiId, boolean ativa, long versao, Instant criadoEm, Instant atualizadoEm) {
        return new UnidadeOrganizacionalPersistida(id, "COD-01", "Unidade válida", null, null,
                TipoUnidade.OUTRA, paiId, null, null, ativa,
                criadoEm, "ator", atualizadoEm, "ator", versao);
    }

    private static void assertInvalido(Runnable operacao, String campo) {
        assertThatThrownBy(operacao::run)
                .isInstanceOf(DadoUnidadeInvalidoException.class)
                .extracting("codigoErro", "campo")
                .containsExactly("UNIDADE-0002", campo);
    }
}
