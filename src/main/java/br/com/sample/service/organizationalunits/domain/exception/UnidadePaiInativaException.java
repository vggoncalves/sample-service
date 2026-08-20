package br.com.sample.service.organizationalunits.domain.exception;

public final class UnidadePaiInativaException extends UnidadeOrganizacionalException {
    public UnidadePaiInativaException() {
        super("UNIDADE-0006", "Uma unidade ativa não pode ser vinculada a uma unidade pai inativa", "unidadePaiId");
    }
}
