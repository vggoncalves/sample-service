package br.com.sample.service.organizationalunits.application;

public final class CodigoUnidadeDuplicadoException extends RuntimeException {
    public CodigoUnidadeDuplicadoException() {
        super("Já existe uma unidade com o código informado");
    }
}
