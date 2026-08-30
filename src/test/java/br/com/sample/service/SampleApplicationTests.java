package br.com.sample.service;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
@ActiveProfiles("local-postgres")
@SpringBootTest
class SampleApplicationTests {
    private static final String SENHA_ADMIN_LOCAL = UUID.randomUUID().toString();
    private static final String SENHA_CONSULTA_LOCAL = UUID.randomUUID().toString();

    @Container
    @ServiceConnection
    @SuppressWarnings("resource")
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17-alpine")
                    .withDatabaseName("sample_test")
                    .withUsername("sample")
                    .withPassword("sample");

    @DynamicPropertySource
    static void securityProperties(DynamicPropertyRegistry registry) {
        registry.add("SAMPLE_LOCAL_ADMIN_PASSWORD", () -> SENHA_ADMIN_LOCAL);
        registry.add("SAMPLE_LOCAL_CONSULTA_PASSWORD", () -> SENHA_CONSULTA_LOCAL);
    }

    @Test
    void contextLoads() {
    }

}
