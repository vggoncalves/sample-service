package br.com.sample.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
@ActiveProfiles("local-postgres")
@SpringBootTest
class SampleApplicationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17-alpine")
                    .withDatabaseName("sample_test")
                    .withUsername("sample")
                    .withPassword("sample");

    @Test
    void contextLoads() {
    }

}
