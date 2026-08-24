package br.com.sample.service.organizationalunits.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.sample.service.organizationalunits.domain.exception.CicloHierarquicoException;
import br.com.sample.service.organizationalunits.domain.exception.DescendenteAtivoException;
import br.com.sample.service.organizationalunits.domain.exception.ReativacaoSobPaiInativoException;
import br.com.sample.service.organizationalunits.domain.exception.UnidadeOrganizacionalException;
import br.com.sample.service.organizationalunits.domain.exception.UnidadePaiInativaException;
import br.com.sample.service.organizationalunits.domain.exception.UnidadePaiInexistenteException;
import br.com.sample.service.organizationalunits.domain.model.DadosBasicosUnidade;
import br.com.sample.service.organizationalunits.domain.model.TipoUnidade;
import br.com.sample.service.organizationalunits.domain.model.UnidadeOrganizacional;
import br.com.sample.service.organizationalunits.domain.model.UnidadeOrganizacionalPersistida;
import br.com.sample.service.organizationalunits.domain.repository.UnidadeOrganizacionalRepository;
import br.com.sample.service.organizationalunits.domain.repository.FiltroUnidadeOrganizacional;
import br.com.sample.service.organizationalunits.domain.repository.PaginaUnidades;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PoliticaHierarquiaUnidadeOrganizacionalTests {
    private static final Instant AGORA = Instant.parse("2026-08-19T12:00:00Z");
    private RepositorioFalso repository;
    private PoliticaHierarquiaUnidadeOrganizacional politica;

    @BeforeEach
    void preparar() {
        repository = new RepositorioFalso();
        politica = new PoliticaHierarquiaUnidadeOrganizacional(repository);
    }

    @Test
    void permiteMultiplasRaizesEFilhaSobPaiAtivo() {
        var raizA = unidade(UUID.randomUUID(), null, true);
        var raizB = unidade(UUID.randomUUID(), null, true);
        repository.adicionar(raizA);

        assertThatCode(() -> politica.validarCriacao(raizA)).doesNotThrowAnyException();
        assertThatCode(() -> politica.validarCriacao(raizB)).doesNotThrowAnyException();
        assertThatCode(() -> politica.validarCriacao(unidade(UUID.randomUUID(), raizA.id(), true)))
                .doesNotThrowAnyException();
    }

    @Test
    void rejeitaPaiInexistenteEAutorreferencia() {
        var paiInexistente = unidade(UUID.randomUUID(), UUID.randomUUID(), true);
        assertErro(() -> politica.validarCriacao(paiInexistente), UnidadePaiInexistenteException.class, "UNIDADE-0005");

        var id = UUID.randomUUID();
        assertErro(() -> politica.validarCriacao(unidade(id, id, true)), CicloHierarquicoException.class, "UNIDADE-0004");
    }

    @Test
    void rejeitaCicloIndiretoAoMoverAncestralSobDescendente() {
        var aId = UUID.randomUUID();
        var b = unidade(UUID.randomUUID(), aId, true);
        var c = unidade(UUID.randomUUID(), b.id(), true);
        repository.adicionar(b, c);
        var a = unidade(aId, null, true);

        assertErro(() -> politica.alterarDadosBasicos(a, dados(c.id()), AGORA.plusSeconds(1), "ator"),
                CicloHierarquicoException.class, "UNIDADE-0004");
        assertThat(a.unidadePaiId()).isNull();
    }

    @Test
    void interrompeTravessiaQuandoHierarquiaPreexistenteTemCiclo() {
        var bId = UUID.randomUUID();
        var cId = UUID.randomUUID();
        repository.adicionar(unidade(bId, cId, true), unidade(cId, bId, true));
        var alvo = unidade(UUID.randomUUID(), null, true);

        assertErro(() -> politica.alterarDadosBasicos(alvo, dados(bId), AGORA.plusSeconds(1), "ator"),
                CicloHierarquicoException.class, "UNIDADE-0004");
    }

    @Test
    void rejeitaFilhaAtivaSobPaiInativoMasPermiteFilhaInativa() {
        var pai = unidade(UUID.randomUUID(), null, false);
        repository.adicionar(pai);
        assertErro(() -> politica.validarCriacao(unidade(UUID.randomUUID(), pai.id(), true)),
                UnidadePaiInativaException.class, "UNIDADE-0006");

        var filhaInativa = unidade(UUID.randomUUID(), null, false);
        assertThatCode(() -> politica.alterarDadosBasicos(
                filhaInativa, dados(pai.id()), AGORA.plusSeconds(1), "ator")).doesNotThrowAnyException();
        assertThat(filhaInativa.unidadePaiId()).isEqualTo(pai.id());
    }

    @Test
    void reativaSomenteComPaiAtivoOuComoRaiz() {
        var paiInativo = unidade(UUID.randomUUID(), null, false);
        var filha = unidade(UUID.randomUUID(), paiInativo.id(), false);
        repository.adicionar(paiInativo);
        assertErro(() -> politica.reativar(filha, AGORA.plusSeconds(1), "ator"),
                ReativacaoSobPaiInativoException.class, "UNIDADE-0013");
        assertThat(filha.ativa()).isFalse();

        var paiAtivo = unidade(UUID.randomUUID(), null, true);
        var filhaPermitida = unidade(UUID.randomUUID(), paiAtivo.id(), false);
        var raizInativa = unidade(UUID.randomUUID(), null, false);
        repository.adicionar(paiAtivo);
        politica.reativar(filhaPermitida, AGORA.plusSeconds(1), "ator");
        politica.reativar(raizInativa, AGORA.plusSeconds(1), "ator");
        assertThat(filhaPermitida.ativa()).isTrue();
        assertThat(raizInativa.ativa()).isTrue();
    }

    @Test
    void bloqueiaDesativacaoComDescendenteAtivoDiretoOuIndireto() {
        var raiz = unidade(UUID.randomUUID(), null, true);
        var filhaAtiva = unidade(UUID.randomUUID(), raiz.id(), true);
        repository.adicionar(raiz, filhaAtiva);
        assertErro(() -> politica.desativar(raiz, AGORA.plusSeconds(1), "ator"),
                DescendenteAtivoException.class, "UNIDADE-0007");

        repository = new RepositorioFalso();
        politica = new PoliticaHierarquiaUnidadeOrganizacional(repository);
        var filhaInativa = unidade(UUID.randomUUID(), raiz.id(), false);
        var netaAtiva = unidade(UUID.randomUUID(), filhaInativa.id(), true);
        repository.adicionar(raiz, filhaInativa, netaAtiva);
        assertErro(() -> politica.desativar(raiz, AGORA.plusSeconds(1), "ator"),
                DescendenteAtivoException.class, "UNIDADE-0007");
    }

    @Test
    void desativaQuandoTodosOsDescendentesEstaoInativosETrataRepeticaoComoCiclo() {
        var raiz = unidade(UUID.randomUUID(), null, true);
        var filha = unidade(UUID.randomUUID(), raiz.id(), false);
        var neta = unidade(UUID.randomUUID(), filha.id(), false);
        repository.adicionar(raiz, filha, neta);
        politica.desativar(raiz, AGORA.plusSeconds(1), "ator");
        assertThat(raiz.ativa()).isFalse();

        var corrompida = unidade(UUID.randomUUID(), filha.id(), false);
        repository.adicionar(corrompida);
        repository.forcarFilha(corrompida.id(), filha);
        assertErro(() -> politica.desativar(unidade(corrompida.id(), null, true), AGORA.plusSeconds(2), "ator"),
                CicloHierarquicoException.class, "UNIDADE-0004");
    }

    @Test
    void operacoesDeSituacaoJaSatisfeitasSaoIdempotentes() {
        var ativa = unidade(UUID.randomUUID(), null, true);
        politica.reativar(ativa, null, null);
        assertThat(ativa.ativa()).isTrue();

        var inativa = unidade(UUID.randomUUID(), null, false);
        politica.desativar(inativa, null, null);
        assertThat(inativa.ativa()).isFalse();
    }

    private static DadosBasicosUnidade dados(UUID paiId) {
        return new DadosBasicosUnidade("Unidade válida", null, null, TipoUnidade.OUTRA, paiId, null, null);
    }

    private static UnidadeOrganizacional unidade(UUID id, UUID paiId, boolean ativa) {
        return UnidadeOrganizacional.restaurar(new UnidadeOrganizacionalPersistida(
                id, "COD-" + id.toString().substring(0, 8), "Unidade válida", null, null,
                TipoUnidade.OUTRA, paiId, null, null, ativa,
                AGORA, "ator", AGORA, "ator", 0));
    }

    private static void assertErro(
            Runnable operacao, Class<? extends UnidadeOrganizacionalException> tipo, String codigo) {
        assertThatThrownBy(operacao::run).isInstanceOf(tipo).extracting("codigoErro").isEqualTo(codigo);
    }

    private static final class RepositorioFalso implements UnidadeOrganizacionalRepository {
        private final Map<UUID, UnidadeOrganizacional> unidades = new HashMap<>();
        private final Map<UUID, List<UnidadeOrganizacional>> filhasForcadas = new HashMap<>();

        void adicionar(UnidadeOrganizacional... novas) {
            for (var unidade : novas) {
                unidades.put(unidade.id(), unidade);
            }
        }

        void forcarFilha(UUID paiId, UnidadeOrganizacional filha) {
            filhasForcadas.computeIfAbsent(paiId, chave -> new ArrayList<>()).add(filha);
        }

        @Override
        public UnidadeOrganizacional salvar(UnidadeOrganizacional unidade) {
            unidades.put(unidade.id(), unidade);
            return unidade;
        }

        @Override
        public Optional<UnidadeOrganizacional> buscarPorId(UUID id) {
            return Optional.ofNullable(unidades.get(id));
        }

        @Override
        public PaginaUnidades buscar(FiltroUnidadeOrganizacional filtro) {
            throw new UnsupportedOperationException("Consulta paginada não é necessária neste teste de política");
        }

        @Override
        public List<UnidadeOrganizacional> buscarRaizes() {
            return unidades.values().stream().filter(unidade -> unidade.unidadePaiId() == null).toList();
        }

        @Override
        public List<UnidadeOrganizacional> buscarFilhasDiretas(UUID paiId) {
            var resultado = unidades.values().stream().filter(unidade -> paiId.equals(unidade.unidadePaiId())).toList();
            if (!filhasForcadas.containsKey(paiId)) {
                return resultado;
            }
            var combinado = new ArrayList<>(resultado);
            combinado.addAll(filhasForcadas.get(paiId));
            return combinado;
        }

        @Override
        public boolean existePorCodigo(String codigo) {
            return unidades.values().stream().anyMatch(unidade -> unidade.codigo().equals(codigo));
        }
    }
}
