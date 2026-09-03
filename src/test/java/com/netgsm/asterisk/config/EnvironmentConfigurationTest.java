package com.netgsm.asterisk.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnvironmentConfigurationTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void loadsLocalDotenvIntoYamlAndTypedProperties() throws Exception {
        Path dotenv = temporaryDirectory.resolve(".env");
        Files.writeString(dotenv, """
                DB_HOST=local-db.invalid
                DB_PORT=5433
                DB_NAME=local_test
                DB_USERNAME=test_user
                DB_PASSWORD=test-only-password
                JWT_SECRET=test-only-secret
                JWT_EXPIRATION=120000
                SERVER_PORT=9090
                CORS_ALLOWED_ORIGINS=https://first.invalid,https://second.invalid
                """);
        try (var context = start(dotenv, Map.of())) {
            var environment = context.getEnvironment();
            assertThat(environment.getProperty("spring.datasource.url"))
                    .isEqualTo("jdbc:postgresql://local-db.invalid:5433/local_test");
            assertThat(environment.getProperty("spring.datasource.username")).isEqualTo("test_user");
            assertThat(environment.getProperty("spring.datasource.password")).isEqualTo("test-only-password");
            assertThat(environment.getProperty("server.port")).isEqualTo("9090");
            var jwt = context.getBean(JwtProperties.class);
            assertThat(jwt.secret()).isEqualTo("test-only-secret");
            assertThat(jwt.expiration()).isEqualTo(120000);
            assertThat(jwt.toString()).doesNotContain(jwt.secret());
            assertThat(context.getBean(CorsProperties.class).allowedOrigins())
                    .containsExactly("https://first.invalid", "https://second.invalid");
        }
    }

    @Test
    void containerEnvironmentOverridesDotenv() throws Exception {
        Path dotenv = temporaryDirectory.resolve(".env");
        Files.writeString(dotenv, """
                DB_HOST=file-db.invalid
                DB_PORT=5432
                DB_NAME=file_db
                DB_USERNAME=file_user
                DB_PASSWORD=file_password
                JWT_SECRET=file_secret
                JWT_EXPIRATION=1000
                SERVER_PORT=8081
                CORS_ALLOWED_ORIGINS=https://file.invalid
                """);
        try (var context = start(dotenv, containerVariables())) {
            assertContainerValues(context);
        }
    }

    @Test
    void worksWithoutDotenvWhenContainerSuppliesEnvironment() {
        try (var context = start(temporaryDirectory.resolve("missing.env"), containerVariables())) {
            assertContainerValues(context);
            assertThat(context.getEnvironment().getProperty("spring.jpa.hibernate.ddl-auto"))
                    .isEqualTo("validate");
            assertThat(context.getEnvironment().getProperty("spring.jpa.open-in-view"))
                    .isEqualTo("false");
        }
    }

    @Test
    void rejectsNonPositiveJwtExpiration() {
        assertThatThrownBy(() -> {
            try (var ignored = start(temporaryDirectory.resolve("missing.env"),
                    Map.of("JWT_SECRET", "test-only-secret", "JWT_EXPIRATION", "0"))) {
                // A successfully opened context would be a validation regression.
            }
        }).hasRootCauseInstanceOf(org.springframework.boot.context.properties.bind.validation.BindValidationException.class)
                .hasStackTraceContaining("Positive.app.jwt.expiration");
    }

    @Test
    void rejectsBlankJwtSecret() {
        assertThatThrownBy(() -> {
            try (var ignored = start(temporaryDirectory.resolve("missing.env"),
                    Map.of("JWT_SECRET", ""))) {
            }
        }).hasRootCauseInstanceOf(org.springframework.boot.context.properties.bind.validation.BindValidationException.class)
                .hasStackTraceContaining("NotBlank.app.jwt.secret");
    }

    @Test
    void appliesYamlDefaults() {
        try (var context = start(temporaryDirectory.resolve("missing.env"),
                Map.of("JWT_SECRET", "test-only-secret"))) {
            assertThat(context.getBean(JwtProperties.class).expiration()).isEqualTo(86400000);
            assertThat(context.getEnvironment().getProperty("server.port")).isEqualTo("8080");
            assertThat(context.getBean(CorsProperties.class).allowedOrigins())
                    .containsExactly("http://localhost:5173");
        }
    }

    private static Map<String, Object> containerVariables() {
        return Map.of(
                "DB_HOST", "container-db.invalid", "DB_PORT", "5434", "DB_NAME", "container_db",
                "DB_USERNAME", "container_user", "DB_PASSWORD", "container_password",
                "JWT_SECRET", "container_secret", "JWT_EXPIRATION", "90000",
                "SERVER_PORT", "9091", "CORS_ALLOWED_ORIGINS", "https://container.invalid");
    }

    private static void assertContainerValues(ConfigurableApplicationContext context) {
        var environment = context.getEnvironment();
        assertThat(environment.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://container-db.invalid:5434/container_db");
        assertThat(environment.getProperty("spring.datasource.username")).isEqualTo("container_user");
        assertThat(environment.getProperty("spring.datasource.password")).isEqualTo("container_password");
        assertThat(environment.getProperty("server.port")).isEqualTo("9091");
        assertThat(context.getBean(JwtProperties.class).secret()).isEqualTo("container_secret");
        assertThat(context.getBean(JwtProperties.class).expiration()).isEqualTo(90000);
        assertThat(context.getBean(CorsProperties.class).allowedOrigins())
                .containsExactly("https://container.invalid");
    }

    private ConfigurableApplicationContext start(Path dotenv, Map<String, Object> variables) {
        var environment = new StandardEnvironment();
        // Isolate tests from the developer's secrets and use actual OS property-source semantics.
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        environment.getPropertySources().replace(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                new SystemEnvironmentPropertySource(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                        variables));
        var application = new SpringApplication(ConfigurationUnderTest.class);
        application.setEnvironment(environment);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.setLogStartupInfo(false);
        return application.run(
                "--spring.config.location=classpath:/application.yml",
                "--spring.config.import=optional:" + dotenv.toUri() + "[.properties]",
                "--spring.main.banner-mode=off");
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties({JwtProperties.class, CorsProperties.class})
    static class ConfigurationUnderTest {
    }
}
