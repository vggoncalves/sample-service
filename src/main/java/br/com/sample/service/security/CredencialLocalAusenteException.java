package br.com.sample.service.security;

public final class CredencialLocalAusenteException extends RuntimeException {
    public CredencialLocalAusenteException(String propriedade) {
        super("A credencial local obrigatória não foi configurada: " + propriedade);
    }
}
