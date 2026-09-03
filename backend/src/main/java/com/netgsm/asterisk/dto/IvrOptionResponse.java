package com.netgsm.asterisk.dto;
public record IvrOptionResponse(Long id, Long tenantId, Long ivrId, String digit, String actionType, Long targetId) {
}
