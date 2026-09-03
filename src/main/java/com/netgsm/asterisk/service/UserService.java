package com.netgsm.asterisk.service;
import com.netgsm.asterisk.dto.CreateUserRequest;
import com.netgsm.asterisk.dto.UpdateUserRequest;
import com.netgsm.asterisk.dto.UserResponse;
import com.netgsm.asterisk.enums.Role;
import com.netgsm.asterisk.entity.User;
import com.netgsm.asterisk.repository.UserRepository;
import com.netgsm.asterisk.exception.BusinessRuleException;
import com.netgsm.asterisk.exception.DatabaseOperationException;
import com.netgsm.asterisk.exception.DuplicateResourceException;
import com.netgsm.asterisk.exception.GlobalExceptionHandler;
import com.netgsm.asterisk.exception.InvalidCredentialsException;
import com.netgsm.asterisk.exception.PlatformException;
import com.netgsm.asterisk.exception.ResourceNotFoundException;
import com.netgsm.asterisk.exception.TenantAccessDeniedException;
import com.netgsm.asterisk.repository.TenantRepository;
import java.util.Locale;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor @Slf4j @Transactional @PreAuthorize("hasRole('SUPER_ADMIN')")
public class UserService {
    private final UserRepository repository;
    private final TenantRepository tenants;
    private final PasswordEncoder passwords;
    @Transactional(readOnly = true)
    public Page<UserResponse> list(Long tenantId, Pageable page) {
        requireTenant(tenantId); return repository.findAllByTenantId(tenantId, page).map(UserResponse::from);
    }
    @Transactional(readOnly = true)
    public UserResponse get(Long id) { return UserResponse.from(find(id)); }
    public UserResponse create(Long tenantId, CreateUserRequest request) {
        requireTenant(tenantId);
        String username = normalize(request.username()), email = normalize(request.email());
        if (repository.existsByUsername(username) || repository.existsByEmail(email)) throw new DuplicateResourceException("User");
        User user = new User(); user.setTenantId(tenantId); user.setRole(Role.TENANT_ADMIN);
        user.setUsername(username); user.setEmail(email); user.setEnabled(request.enabled());
        user.setPasswordHash(hash(request.password())); repository.saveAndFlush(user);
        log.info("User created id={} tenantId={}", user.getId(), tenantId); return UserResponse.from(user);
    }
    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = find(id);
        if (user.getRole() == Role.SUPER_ADMIN) throw new BusinessRuleException("This API manages tenant users only");
        String username = normalize(request.username()), email = normalize(request.email());
        if (repository.existsByUsernameAndIdNot(username, id) || repository.existsByEmailAndIdNot(email, id))
            throw new DuplicateResourceException("User");
        user.setUsername(username); user.setEmail(email); user.setEnabled(request.enabled());
        if (request.password() != null) user.setPasswordHash(hash(request.password()));
        user.setAuthVersion(user.getAuthVersion() + 1); repository.flush();
        log.info("User updated id={}", id); return UserResponse.from(user);
    }
    public void delete(Long id) {
        User user = find(id);
        if (user.getRole() == Role.SUPER_ADMIN) throw new BusinessRuleException("This API manages tenant users only");
        user.setEnabled(false); user.setAuthVersion(user.getAuthVersion() + 1); log.info("User disabled id={}", id);
    }
    private User find(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User")); }
    private void requireTenant(Long id) {
        if (!tenants.existsById(id)) throw new ResourceNotFoundException("Tenant");
    }
    private String normalize(String value) { return value.trim().toLowerCase(Locale.ROOT); }
    private String hash(String value) {
        if (value.getBytes(StandardCharsets.UTF_8).length > 72) throw new BusinessRuleException("Password exceeds 72 UTF-8 bytes");
        return passwords.encode(value);
    }
}
