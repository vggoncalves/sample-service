package br.com.sample.service.organizationalunits.application;

public final class ArvoreUnidadeInvalidaException extends RuntimeException {
    public ArvoreUnidadeInvalidaException() { super("A profundidade da árvore deve estar entre 1 e 20"); }
}
