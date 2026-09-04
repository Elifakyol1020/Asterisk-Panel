package com.netgsm.asterisk.service.provisioning;

import com.netgsm.asterisk.asterisk.realtime.entity.PsAor;
import com.netgsm.asterisk.asterisk.realtime.entity.PsAuth;
import com.netgsm.asterisk.asterisk.realtime.entity.PsEndpoint;
import com.netgsm.asterisk.asterisk.realtime.entity.PsEndpointIdIp;
import com.netgsm.asterisk.asterisk.realtime.repository.PsAorRepository;
import com.netgsm.asterisk.asterisk.realtime.repository.PsAuthRepository;
import com.netgsm.asterisk.asterisk.realtime.repository.PsEndpointIdIpRepository;
import com.netgsm.asterisk.asterisk.realtime.repository.PsEndpointRepository;
import com.netgsm.asterisk.entity.Trunk;
import com.netgsm.asterisk.exception.BusinessRuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsteriskTrunkProvisioningService {
    private final AsteriskNaming naming;
    private final PsEndpointRepository endpoints;
    private final PsAuthRepository auths;
    private final PsAorRepository aors;
    private final PsEndpointIdIpRepository identifies;
    private final AsteriskRealtimeWriter realtimeWriter;

    public void upsert(Trunk trunk, String sipPassword) {
        validateIpOrCidr(trunk.getHost());
        String id = naming.trunk(trunk.getTenantId(), trunk.getName());
        String authId = naming.trunkAuth(trunk.getTenantId(), trunk.getName());
        log.info("Creating Asterisk trunk: {}", id);

        PsAor aor = new PsAor();
        aor.setId(id);
        aor.setContact("sip:" + trunk.getHost() + ":" + trunk.getPort());
        aor.setQualifyFrequency(60);
        realtimeWriter.upsertAor(aor);

        if (sipPassword != null) {
            PsAuth auth = new PsAuth();
            auth.setId(authId);
            auth.setAuthType("userpass");
            auth.setUsername(trunk.getUsername());
            auth.setPassword(sipPassword);
            realtimeWriter.upsertAuth(auth);
        }

        PsEndpoint endpoint = new PsEndpoint();
        endpoint.setId(id);
        endpoint.setTransport("transport-udp");
        endpoint.setAors(id);
        boolean hasAuth = sipPassword != null || auths.existsById(authId);
        endpoint.setAuth(hasAuth ? authId : null);
        endpoint.setOutboundAuth(hasAuth ? authId : null);
        endpoint.setContext(naming.routerContext());
        endpoint.setDisallow("all");
        endpoint.setAllow("ulaw,alaw");
        endpoint.setDirectMedia("no");
        endpoint.setFromUser(trunk.getFromUser());
        endpoint.setFromDomain(trunk.getFromDomain());
        endpoint.setSetVar("TENANT_ID=" + trunk.getTenantId());
        realtimeWriter.upsertEndpoint(endpoint);

        PsEndpointIdIp identify = new PsEndpointIdIp();
        identify.setId(naming.trunkIdentify(trunk.getTenantId(), trunk.getName()));
        identify.setEndpoint(id);
        identify.setMatchValue(trunk.getHost());
        realtimeWriter.upsertIdentify(identify);
    }

    public void renameOrDelete(Long tenantId, String oldName) {
        endpoints.deleteById(naming.trunk(tenantId, oldName));
        auths.deleteById(naming.trunkAuth(tenantId, oldName));
        aors.deleteById(naming.trunk(tenantId, oldName));
        identifies.deleteById(naming.trunkIdentify(tenantId, oldName));
    }

    private void validateIpOrCidr(String value) {
        if (!value.matches("([0-9]{1,3}\\.){3}[0-9]{1,3}(/[0-9]{1,2})?")) {
            throw new BusinessRuleException("Trunk host must be an IP address or CIDR for Asterisk identify");
        }
        String ip = value.split("/", 2)[0];
        for (String part : ip.split("\\.")) if (Integer.parseInt(part) > 255)
            throw new BusinessRuleException("Trunk host must be an IP address or CIDR for Asterisk identify");
    }
}
