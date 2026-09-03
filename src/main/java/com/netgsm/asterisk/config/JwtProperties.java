package com.netgsm.asterisk.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(@NotBlank String secret, @Positive long expiration) {
    // Expiration is expressed in milliseconds. Never expose the secret via toString.
    @Override
    public String toString() {
        return "JwtProperties[secret=REDACTED, expiration=" + expiration + "]";
    }
}
