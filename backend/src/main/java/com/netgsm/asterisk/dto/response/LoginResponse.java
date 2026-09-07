package com.netgsm.asterisk.dto.response;
public record LoginResponse(String accessToken, String tokenType) {
    @Override public String toString() { return "LoginResponse[REDACTED]"; }
}
