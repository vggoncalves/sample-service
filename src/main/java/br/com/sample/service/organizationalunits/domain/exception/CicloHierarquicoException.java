package br.com.sample.service.organizationalunits.domain.exception;

public final class CicloHierarquicoException extends UnidadeOrganizacionalException {
    public CicloHierarquicoException() {
        super("UNIDADE-0004", "A hierarquia da unidade organizacional não pode conter ciclos");
    }
}
