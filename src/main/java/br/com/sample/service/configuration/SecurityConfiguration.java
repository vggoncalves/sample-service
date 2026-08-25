package br.com.sample.service.configuration;

import br.com.sample.service.security.CredencialLocalAusenteException;
import br.com.sample.service.security.Permissao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.core.env.Environment;

@Configuration
@EnableMethodSecurity
@Profile({"local-sqlite", "local-postgres"})
public class SecurityConfiguration {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) {
        try {
            return http.csrf(Customizer.withDefaults())
                    .authorizeHttpRequests(registry -> registry.requestMatchers("/actuator/health/**").permitAll()
                            .anyRequest().authenticated())
                    .httpBasic(Customizer.withDefaults()).build();
        } catch (Exception exception) {
            throw new SecurityFilterChainConfigurationException(exception);
        }
    }

    @Bean
    UserDetailsService usuariosLocais(Environment environment) {
        var administrador = User.withUsername("admin").password(senhaCodificada(environment, "SAMPLE_LOCAL_ADMIN_PASSWORD"))
                .authorities(
                Permissao.UNIDADE_CONSULTAR.name(), Permissao.UNIDADE_CRIAR.name(), Permissao.UNIDADE_ALTERAR.name(),
                Permissao.UNIDADE_DESATIVAR.name()).build();
        var consulta = User.withUsername("consulta").password(senhaCodificada(environment, "SAMPLE_LOCAL_CONSULTA_PASSWORD"))
                .authorities(Permissao.UNIDADE_CONSULTAR.name()).build();
        return new InMemoryUserDetailsManager(administrador, consulta);
    }

    private static String senhaCodificada(Environment environment, String propriedade) {
        var senha = environment.getProperty(propriedade);
        if (senha == null || senha.isBlank()) {
            throw new CredencialLocalAusenteException(propriedade);
        }
        return "{noop}" + senha;
    }
}
