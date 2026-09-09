package com.netgsm.asterisk.dto.request;
import jakarta.validation.constraints.*;
public record InboundRouteRequest(Long tenantId, @NotBlank @Size(max=120) String name,
 @NotNull @Positive Long trunkId, @NotBlank @Pattern(regexp="[+]?[0-9]{1,20}") String did,
 @NotBlank @Pattern(regexp="ENDPOINT|QUEUE|IVR") String targetType, @NotNull @Positive Long targetId,
 @NotNull Boolean enabled) {}
