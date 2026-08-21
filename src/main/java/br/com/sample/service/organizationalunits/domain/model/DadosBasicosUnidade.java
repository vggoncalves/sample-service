package br.com.sample.service.organizationalunits.domain.model;

import br.com.sample.service.organizationalunits.domain.exception.DadoUnidadeInvalidoException;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

public record DadosBasicosUnidade(
        String nome,
        String sigla,
        String descricao,
        TipoUnidade tipo,
        UUID unidadePaiId,
        String emailContato,
        String telefone) {

    private static final String PREFIXO_CAMPO = "O campo ";
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]++@[^\\s@.]++\\.[^\\s@]++$");
    private static final Pattern PADRAO_TELEFONE = Pattern.compile("^\\+[1-9]\\d{7,14}$");

    public DadosBasicosUnidade {
        nome = obrigatorio(nome, "nome", 3, 150);
        sigla = opcional(sigla, "sigla", 30);
        sigla = sigla == null ? null : sigla.toUpperCase(Locale.ROOT);
        descricao = opcional(descricao, "descricao", 500);
        if (tipo == null) {
            throw invalido("tipo", "O tipo da unidade é obrigatório");
        }
        emailContato = opcional(emailContato, "emailContato", 254);
        emailContato = emailContato == null ? null : emailContato.toLowerCase(Locale.ROOT);
        if (emailContato != null && !EMAIL.matcher(emailContato).matches()) {
            throw invalido("emailContato", "O e-mail de contato possui formato inválido");
        }
        telefone = normalizarTelefone(telefone);
    }

    private static String normalizarTelefone(String valor) {
        var normalizado = opcional(valor, "telefone", 64);
        if (normalizado == null) {
            return null;
        }
        normalizado = normalizado.replaceAll("[\\s()\\-]", "");
        if (!PADRAO_TELEFONE.matcher(normalizado).matches()) {
            throw invalido("telefone", "O telefone deve estar no formato E.164");
        }
        return normalizado;
    }

    private static String obrigatorio(String valor, String campo, int minimo, int maximo) {
        if (valor == null) {
            throw invalido(campo, PREFIXO_CAMPO + campo + " é obrigatório");
        }
        var normalizado = valor.trim();
        if (normalizado.length() < minimo || normalizado.length() > maximo) {
            throw invalido(campo, PREFIXO_CAMPO + campo + " deve possuir entre " + minimo + " e " + maximo + " caracteres");
        }
        return normalizado;
    }

    private static String opcional(String valor, String campo, int maximo) {
        if (valor == null) {
            return null;
        }
        var normalizado = valor.trim();
        if (normalizado.isEmpty()) {
            return null;
        }
        if (normalizado.length() > maximo) {
            throw invalido(campo, PREFIXO_CAMPO + campo + " deve possuir no máximo " + maximo + " caracteres");
        }
        return normalizado;
    }

    private static DadoUnidadeInvalidoException invalido(String campo, String mensagem) {
        return new DadoUnidadeInvalidoException(campo, mensagem);
    }
}
