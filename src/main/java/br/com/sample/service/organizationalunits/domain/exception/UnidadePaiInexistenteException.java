package br.com.sample.service.organizationalunits.domain.exception;

import java.util.UUID;

public final class UnidadePaiInexistenteException extends UnidadeOrganizacionalException {
    public UnidadePaiInexistenteException(UUID unidadePaiId) {
        super("UNIDADE-0005", "A unidade pai informada não existe: " + unidadePaiId, "unidadePaiId");
    }
}
