package br.com.sample.service.organizationalunits.domain.model;

import br.com.sample.service.organizationalunits.domain.exception.DadoUnidadeInvalidoException;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

public final class UnidadeOrganizacional {

    private static final Pattern CODIGO = Pattern.compile("^[A-Z0-9][A-Z0-9._-]{1,49}$");

    private final UUID id;
    private final String codigo;
    private final Instant criadoEm;
    private final String criadoPor;
    private DadosBasicosUnidade dados;
    private boolean ativa;
    private Instant atualizadoEm;
    private String atualizadoPor;
    private final long versao;

    private UnidadeOrganizacional(UnidadeOrganizacionalPersistida estado) {
        if (estado.id() == null) {
            throw invalido("id", "O identificador é obrigatório");
        }
        id = estado.id();
        codigo = normalizarCodigo(estado.codigo());
        dados = new DadosBasicosUnidade(estado.nome(), estado.sigla(), estado.descricao(), estado.tipo(),
                estado.unidadePaiId(), estado.emailContato(), estado.telefone());
        ativa = estado.ativa();
        criadoEm = instanteObrigatorio(estado.criadoEm(), "criadoEm");
        criadoPor = atorObrigatorio(estado.criadoPor(), "criadoPor");
        atualizadoEm = instanteObrigatorio(estado.atualizadoEm(), "atualizadoEm");
        atualizadoPor = atorObrigatorio(estado.atualizadoPor(), "atualizadoPor");
        if (atualizadoEm.isBefore(criadoEm)) {
            throw invalido("atualizadoEm", "A atualização não pode preceder a criação");
        }
        if (estado.versao() < 0) {
            throw invalido("versao", "A versão não pode ser negativa");
        }
        versao = estado.versao();
    }

    public static UnidadeOrganizacional criar(
            String codigo, DadosBasicosUnidade dados, Instant instante, String ator) {
        if (dados == null) {
            throw invalido("dados", "Os dados básicos são obrigatórios");
        }
        var agora = instanteObrigatorio(instante, "criadoEm");
        var atorNormalizado = atorObrigatorio(ator, "criadoPor");
        return new UnidadeOrganizacional(new UnidadeOrganizacionalPersistida(
                UUID.randomUUID(), codigo, dados.nome(), dados.sigla(), dados.descricao(), dados.tipo(),
                dados.unidadePaiId(), dados.emailContato(), dados.telefone(), true,
                agora, atorNormalizado, agora, atorNormalizado, 0));
    }

    public static UnidadeOrganizacional restaurar(UnidadeOrganizacionalPersistida estado) {
        if (estado == null) {
            throw invalido("estado", "O estado persistido é obrigatório");
        }
        return new UnidadeOrganizacional(estado);
    }

    public void alterarDadosBasicos(DadosBasicosUnidade novosDados, Instant instante, String ator) {
        if (novosDados == null) {
            throw invalido("dados", "Os dados básicos são obrigatórios");
        }
        registrarAtualizacao(instante, ator);
        dados = novosDados;
    }

    public void desativar(Instant instante, String ator) {
        if (ativa) {
            registrarAtualizacao(instante, ator);
            ativa = false;
        }
    }

    public void reativar(Instant instante, String ator) {
        if (!ativa) {
            registrarAtualizacao(instante, ator);
            ativa = true;
        }
    }

    public UnidadeOrganizacionalPersistida estadoPersistido() {
        return new UnidadeOrganizacionalPersistida(id, codigo, dados.nome(), dados.sigla(), dados.descricao(),
                dados.tipo(), dados.unidadePaiId(), dados.emailContato(), dados.telefone(), ativa,
                criadoEm, criadoPor, atualizadoEm, atualizadoPor, versao);
    }

    public UUID id() { return id; }
    public String codigo() { return codigo; }
    public String nome() { return dados.nome(); }
    public String sigla() { return dados.sigla(); }
    public String descricao() { return dados.descricao(); }
    public TipoUnidade tipo() { return dados.tipo(); }
    public UUID unidadePaiId() { return dados.unidadePaiId(); }
    public String emailContato() { return dados.emailContato(); }
    public String telefone() { return dados.telefone(); }
    public boolean ativa() { return ativa; }
    public Instant criadoEm() { return criadoEm; }
    public String criadoPor() { return criadoPor; }
    public Instant atualizadoEm() { return atualizadoEm; }
    public String atualizadoPor() { return atualizadoPor; }
    public long versao() { return versao; }

    private void registrarAtualizacao(Instant instante, String ator) {
        var novaData = instanteObrigatorio(instante, "atualizadoEm");
        if (novaData.isBefore(criadoEm)) {
            throw invalido("atualizadoEm", "A atualização não pode preceder a criação");
        }
        var novoAtor = atorObrigatorio(ator, "atualizadoPor");
        atualizadoEm = novaData;
        atualizadoPor = novoAtor;
    }

    private static String normalizarCodigo(String valor) {
        if (valor == null) {
            throw invalido("codigo", "O código é obrigatório");
        }
        var normalizado = valor.trim().toUpperCase(Locale.ROOT);
        if (!CODIGO.matcher(normalizado).matches()) {
            throw invalido("codigo", "O código deve possuir de 2 a 50 caracteres válidos");
        }
        return normalizado;
    }

    private static Instant instanteObrigatorio(Instant instante, String campo) {
        if (instante == null) {
            throw invalido(campo, "O instante de auditoria é obrigatório");
        }
        return instante;
    }

    private static String atorObrigatorio(String ator, String campo) {
        if (ator == null || ator.trim().isEmpty() || ator.trim().length() > 255) {
            throw invalido(campo, "O ator de auditoria deve possuir de 1 a 255 caracteres");
        }
        return ator.trim();
    }

    private static DadoUnidadeInvalidoException invalido(String campo, String mensagem) {
        return new DadoUnidadeInvalidoException(campo, mensagem);
    }
}
