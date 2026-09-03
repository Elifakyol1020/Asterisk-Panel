package com.netgsm.asterisk.security;
import com.netgsm.asterisk.entity.Role;
public record CurrentUser(Long userId, Long tenantId, Role role, String username) { }
