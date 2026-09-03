package com.netgsm.asterisk.service;

import com.netgsm.asterisk.dto.LoginRequest;
import com.netgsm.asterisk.dto.LoginResponse;
import com.netgsm.asterisk.service.JwtService;
import com.netgsm.asterisk.exception.InvalidCredentialsException;
import com.netgsm.asterisk.security.CurrentUser;
import com.netgsm.asterisk.entity.TenantStatus;
import com.netgsm.asterisk.repository.TenantRepository;
import com.netgsm.asterisk.entity.Role;
import com.netgsm.asterisk.entity.User;
import com.netgsm.asterisk.repository.UserRepository;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional(readOnly = true)
public class AuthService {
    private final UserRepository users;
    private final TenantRepository tenants;
    private final PasswordEncoder passwords;
    private final JwtService tokens;
    private final String dummyHash;
    public AuthService(UserRepository users, TenantRepository tenants, PasswordEncoder passwords, JwtService tokens) {
        this.users = users; this.tenants = tenants; this.passwords = passwords; this.tokens = tokens;
        dummyHash = passwords.encode(java.util.UUID.randomUUID().toString());
    }
    public LoginResponse login(LoginRequest request) {
        var user = users.findByUsername(request.username().trim().toLowerCase(Locale.ROOT)).orElse(null);
        boolean matches = passwords.matches(request.password(), user == null ? dummyHash : user.getPasswordHash());
        if (!matches || user == null || !active(user)) throw new InvalidCredentialsException();
        return new LoginResponse(tokens.issue(user), "Bearer",
                new LoginResponse.UserInfo(user.getId(), user.getUsername(), user.getRole(), user.getTenantId()));
    }
    public CurrentUser authenticate(Jwt jwt) {
        Long id = Long.valueOf(jwt.getSubject());
        User user = users.findById(id).orElseThrow(InvalidCredentialsException::new);
        Number version = jwt.getClaim("ver");
        Number userId = jwt.getClaim("userId");
        Number tenant = jwt.getClaim("tenantId");
        if (!active(user) || userId == null || userId.longValue() != id || version == null
                || version.longValue() != user.getAuthVersion()
                || !user.getRole().name().equals(jwt.getClaimAsString("role"))
                || !java.util.Objects.equals(user.getTenantId(), tenant == null ? null : tenant.longValue()))
            throw new InvalidCredentialsException();
        return new CurrentUser(id, user.getTenantId(), user.getRole(), user.getUsername());
    }
    private boolean active(User user) {
        if (!user.isEnabled()) return false;
        if (user.getRole() == Role.SUPER_ADMIN) return user.getTenantId() == null;
        return user.getTenantId() != null && tenants.findById(user.getTenantId())
                .map(t -> t.getStatus() == TenantStatus.ACTIVE).orElse(false);
    }
}
