package br.com.sample.service.organizationalunits.domain.exception;

public final class DescendenteAtivoException extends UnidadeOrganizacionalException {
    public DescendenteAtivoException() {
        super("UNIDADE-0007", "A unidade não pode ser desativada enquanto possuir descendente ativo", "ativa");
    }
}
