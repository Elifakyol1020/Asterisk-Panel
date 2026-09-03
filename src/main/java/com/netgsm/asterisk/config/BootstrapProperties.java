package com.netgsm.asterisk.config;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties("app.bootstrap")
public record BootstrapProperties(String username, String email, String password) {
    @Override public String toString() { return "BootstrapProperties[REDACTED]"; }
}
