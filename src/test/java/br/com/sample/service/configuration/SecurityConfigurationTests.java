package br.com.sample.service.configuration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class SecurityConfigurationTests {
    private final SecurityConfiguration configuration = new SecurityConfiguration();

    @Test
    void codificaCredenciaisLocaisComBcrypt() {
        var environment = new MockEnvironment()
                .withProperty("SAMPLE_LOCAL_ADMIN_PASSWORD", "senha-de-teste-admin")
                .withProperty("SAMPLE_LOCAL_CONSULTA_PASSWORD", "senha-de-teste-consulta");
        var passwordEncoder = configuration.passwordEncoder();
        var usuarios = configuration.usuariosLocais(environment, passwordEncoder);
        var administrador = usuarios.loadUserByUsername("admin");

        assertTrue(administrador.getPassword().startsWith("$2"));
        assertFalse(administrador.getPassword().contains("senha-de-teste-admin"));
        assertTrue(passwordEncoder.matches("senha-de-teste-admin", administrador.getPassword()));
    }
}
