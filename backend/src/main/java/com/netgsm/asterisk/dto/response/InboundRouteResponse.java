package com.netgsm.asterisk.dto.response;
public record InboundRouteResponse(Long id, Long tenantId, String name, Long trunkId, String did,
 String targetType, Long targetId, Boolean enabled, String context) {}
