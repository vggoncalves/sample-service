package br.com.sample.service.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"local-sqlite", "local-postgres"})
public class OpenApiConfiguration {
    @Bean
    OpenAPI openApi() {
        return new OpenAPI().info(new Info().title("API de Unidades Organizacionais").version("v1.0.0")
                .description("API protegida para consulta e manutenção de unidades organizacionais."))
                .components(new Components().addSecuritySchemes("basicAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")
                                .description("Autenticação local exclusiva para desenvolvimento.")))
                .addSecurityItem(new SecurityRequirement().addList("basicAuth"));
    }
}
