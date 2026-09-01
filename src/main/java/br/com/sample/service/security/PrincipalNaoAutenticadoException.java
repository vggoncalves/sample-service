package br.com.sample.service.security;

import org.springframework.security.access.AccessDeniedException;

public final class PrincipalNaoAutenticadoException extends AccessDeniedException {
    public PrincipalNaoAutenticadoException() {
        super("Não há um principal autenticado no contexto de segurança.");
    }
}
