package br.com.sample.service.organizationalunits.application;

import java.util.UUID;

public final class UnidadeOrganizacionalNaoEncontradaException extends RuntimeException {
    public UnidadeOrganizacionalNaoEncontradaException(UUID id) {
        super("A unidade organizacional não existe: " + id);
    }
}
