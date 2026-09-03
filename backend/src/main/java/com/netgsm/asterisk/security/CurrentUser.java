package com.netgsm.asterisk.security;
import com.netgsm.asterisk.enums.Role;
public record CurrentUser(Long userId, Long tenantId, Role role, String username) { }
