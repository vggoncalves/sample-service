package br.com.sample.service.security;

public final class PrincipalNaoAutenticadoException extends RuntimeException {
    public PrincipalNaoAutenticadoException() {
        super("Não há um principal autenticado no contexto de segurança.");
    }
}
