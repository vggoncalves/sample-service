package br.com.sample.service.organizationalunits.application;

public final class ConsultaUnidadeInvalidaException extends RuntimeException {
    public ConsultaUnidadeInvalidaException(String mensagem) {
        super(mensagem);
    }
}
