package com.netgsm.asterisk.service.provisioning;

import java.text.Normalizer;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
@lombok.RequiredArgsConstructor
public class AsteriskNaming {
    private final com.netgsm.asterisk.repository.TenantRepository tenants;

    public String endpoint(Long tenantId, String extension) {
        var tenant = tenants.findById(tenantId).orElseThrow(() ->
                new com.netgsm.asterisk.exception.ResourceNotFoundException("Tenant"));
        if (!tenant.getCode().matches("[0-9]{4,6}")) {
            throw new com.netgsm.asterisk.exception.BusinessRuleException("Tenant numarası için 005 migration uygulanmalıdır");
        }
        return extension + "-" + tenant.getCode();
    }

    public String endpointAuth(Long tenantId, String extension) {
        return endpoint(tenantId, extension) + "_auth";
    }

    public String trunk(Long tenantId, String name) {
        return tenantPrefix(tenantId) + "_trunk_" + safe(name);
    }

    public String trunkAuth(Long tenantId, String name) {
        return trunk(tenantId, name) + "_auth";
    }

    public String trunkIdentify(Long tenantId, String name) {
        return trunk(tenantId, name) + "_identify";
    }

    public String queue(Long tenantId, String name) {
        return tenantPrefix(tenantId) + "_" + safe(name);
    }

    public String ivrContext(Long tenantId, String name) {
        return tenantPrefix(tenantId) + "_ivr_" + safe(name);
    }

    /** Realtime dialplan context isolated to one tenant. */
    public String tenantContext(Long tenantId) {
        return "tenant_" + tenantId + "_internal";
    }

    public String inboundContext(Long tenantId, Long trunkId) { return "tenant_" + tenantId + "_inbound_" + trunkId; }

    public String tenantPrefix(Long tenantId) {
        return "tenant" + tenantId;
    }

    public String safe(String value) {
        String normalized = Normalizer.normalize(value.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('ı', 'i').replace('ğ', 'g').replace('ü', 'u')
                .replace('ş', 's').replace('ö', 'o').replace('ç', 'c')
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
        if (normalized.isBlank()) normalized = "item";
        return normalized.length() > 32 ? normalized.substring(0, 32).replaceAll("_+$", "") : normalized;
    }
}
