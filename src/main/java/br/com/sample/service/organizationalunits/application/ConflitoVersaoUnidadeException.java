package br.com.sample.service.organizationalunits.application;

public final class ConflitoVersaoUnidadeException extends RuntimeException {
    public ConflitoVersaoUnidadeException() {
        super("A versão informada não corresponde à versão atual da unidade");
    }
}
