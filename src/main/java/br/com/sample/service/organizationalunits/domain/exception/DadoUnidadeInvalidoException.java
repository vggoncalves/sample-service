package br.com.sample.service.organizationalunits.domain.exception;

public final class DadoUnidadeInvalidoException extends UnidadeOrganizacionalException {
    public DadoUnidadeInvalidoException(String campo, String mensagem) {
        super("UNIDADE-0002", mensagem, campo);
    }
}
