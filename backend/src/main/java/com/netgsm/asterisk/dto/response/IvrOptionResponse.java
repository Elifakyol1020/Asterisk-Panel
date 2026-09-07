package com.netgsm.asterisk.dto.response;
public record IvrOptionResponse(Long id, Long tenantId, Long ivrId, String digit, String actionType, Long targetId) {
}
