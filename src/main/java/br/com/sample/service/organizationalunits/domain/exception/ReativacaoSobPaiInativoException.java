package br.com.sample.service.organizationalunits.domain.exception;

public final class ReativacaoSobPaiInativoException extends UnidadeOrganizacionalException {
    public ReativacaoSobPaiInativoException() {
        super("UNIDADE-0013", "A unidade não pode ser reativada sob uma unidade pai inativa", "ativa");
    }
}
