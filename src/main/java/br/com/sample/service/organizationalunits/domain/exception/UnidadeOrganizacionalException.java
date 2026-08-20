package br.com.sample.service.organizationalunits.domain.exception;

public abstract class UnidadeOrganizacionalException extends RuntimeException {
    private final String codigoErro;
    private final String campo;

    protected UnidadeOrganizacionalException(String codigoErro, String mensagem) {
        this(codigoErro, mensagem, null);
    }

    protected UnidadeOrganizacionalException(String codigoErro, String mensagem, String campo) {
        super(mensagem);
        this.codigoErro = codigoErro;
        this.campo = campo;
    }

    public String getCodigoErro() { return codigoErro; }
    public String getCampo() { return campo; }
}
