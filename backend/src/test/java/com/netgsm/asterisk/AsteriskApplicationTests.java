package com.netgsm.asterisk;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import com.netgsm.asterisk.config.JwtProperties;
import com.netgsm.asterisk.config.CorsProperties;
import static org.assertj.core.api.Assertions.assertThat;

// H2 is test-only; the production PostgreSQL configuration remains validate-only.
@SpringBootTest(properties = {
        "spring.config.import=",
        "spring.datasource.url=jdbc:h2:mem:platform;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS platform",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.jwt.secret=test-only-secret-that-is-at-least-32-bytes",
        "app.jwt.expiration=60000",
        "app.cors.allowed-origins=https://test.invalid"
})
class AsteriskApplicationTests {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private CorsProperties corsProperties;

    @Test
    void contextLoads() {
        assertThat(jwtProperties.expiration()).isEqualTo(60000);
        assertThat(corsProperties.allowedOrigins()).containsExactly("https://test.invalid");
    }

}
