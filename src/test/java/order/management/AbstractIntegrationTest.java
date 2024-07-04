package order.management;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
public class AbstractIntegrationTest {
    @Container
    public static PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:13.11")
                    .withDatabaseName("order_management")
                    .withUsername("postgres")
                    .withPassword("secret")
                    .withInitScript("schema.sql");

    @BeforeAll
    public static void setUp() {
        postgresContainer.start();

        System.setProperty("POSTGRES_URL", postgresContainer.getJdbcUrl());
        System.setProperty("POSTGRES_USER", postgresContainer.getUsername());
        System.setProperty("POSTGRES_PASSWORD", postgresContainer.getPassword());
    }

    @AfterAll
    public static void tearDown() {
        postgresContainer.stop();
    }

}
