package com.netgsm.asterisk.service;

import com.netgsm.asterisk.config.JwtProperties;
import com.netgsm.asterisk.entity.User;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.List;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Service
public class JwtService {
    private final JwtProperties properties;
    private final JwtEncoder encoder;
    private final NimbusJwtDecoder decoder;
    public JwtService(JwtProperties properties) {
        this.properties = properties;
        byte[] bytes = properties.secret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32 || properties.secret().startsWith("change_this"))
            throw new IllegalStateException("Configure JWT_SECRET with a random secret of at least 32 bytes");
        var key = new SecretKeySpec(bytes, "HmacSHA256");
        encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(Duration.ZERO), new JwtIssuerValidator("asterisk-platform"),
                token -> token.getExpiresAt() != null && token.getIssuedAt() != null
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token"))));
    }
    public String issue(User user) {
        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder().issuer("asterisk-platform")
                .subject(user.getId().toString()).issuedAt(now)
                .expiresAt(now.plusMillis(properties.expiration()))
                .claim("userId", user.getId()).claim("role", user.getRole().name())
                .claim("ver", user.getAuthVersion());
        if (user.getTenantId() != null) claims.claim("tenantId", user.getTenantId());
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims.build())).getTokenValue();
    }
    public Jwt decode(String token) { return decoder.decode(token); }
}
