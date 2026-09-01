package br.com.sample.service.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

class PrincipalSecurityContextTests {
    private final PrincipalSecurityContext principalAtual = new PrincipalSecurityContext();

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void retornaIdentificadorDoPrincipalAutenticado() {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("usuario", "na", List.of()));

        assertEquals("usuario", principalAtual.identificador());
    }

    @Test
    void rejeitaContextoSemAutenticacao() {
        assertThrows(AccessDeniedException.class, principalAtual::identificador);
    }

    @Test
    void rejeitaPrincipalAnonimo() {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken("chave", "anonymous",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

        assertThrows(AccessDeniedException.class, principalAtual::identificador);
    }
}
