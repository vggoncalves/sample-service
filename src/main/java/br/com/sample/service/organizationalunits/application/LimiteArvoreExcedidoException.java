package br.com.sample.service.organizationalunits.application;

public final class LimiteArvoreExcedidoException extends RuntimeException {
    public LimiteArvoreExcedidoException() { super("A árvore excede o limite de 1000 nós"); }
}
