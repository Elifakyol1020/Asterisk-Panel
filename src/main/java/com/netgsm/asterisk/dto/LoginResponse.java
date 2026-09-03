package com.netgsm.asterisk.dto;
import com.netgsm.asterisk.entity.Role;
public record LoginResponse(String accessToken, String tokenType, UserInfo user) {
    public record UserInfo(Long id, String username, Role role, Long tenantId) { }
    @Override public String toString() { return "LoginResponse[REDACTED]"; }
}
