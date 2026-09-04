package com.netgsm.asterisk.service.provisioning;

import com.netgsm.asterisk.asterisk.realtime.entity.AsteriskExtension;
import com.netgsm.asterisk.asterisk.realtime.entity.PsAor;
import com.netgsm.asterisk.asterisk.realtime.entity.PsAuth;
import com.netgsm.asterisk.asterisk.realtime.entity.PsEndpoint;
import com.netgsm.asterisk.asterisk.realtime.repository.AsteriskExtensionRepository;
import com.netgsm.asterisk.asterisk.realtime.repository.AsteriskQueueMemberRepository;
import com.netgsm.asterisk.asterisk.realtime.repository.PsAorRepository;
import com.netgsm.asterisk.asterisk.realtime.repository.PsAuthRepository;
import com.netgsm.asterisk.asterisk.realtime.repository.PsEndpointRepository;
import com.netgsm.asterisk.entity.Endpoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsteriskEndpointProvisioningService {
    private final AsteriskNaming naming;
    private final PsEndpointRepository endpoints;
    private final PsAuthRepository auths;
    private final PsAorRepository aors;
    private final AsteriskExtensionRepository dialplan;
    private final AsteriskQueueMemberRepository queueMembers;
    private final AsteriskRealtimeWriter realtimeWriter;

    public void upsert(Endpoint endpoint, String sipPassword) {
        String id = naming.endpoint(endpoint.getTenantId(), endpoint.getExtension());
        String authId = naming.endpointAuth(endpoint.getTenantId(), endpoint.getExtension());
        String context = naming.tenantContext(endpoint.getTenantId());
        String realtimeExten = endpoint.getExtension();
        log.info("Creating Asterisk endpoint: {}", id);

        dialplan.deleteAllByContextAndExten("realtime", id);

        PsAor aor = new PsAor();
        aor.setId(id);
        aor.setMaxContacts(1);
        aor.setRemoveExisting("yes");
        aor.setQualifyFrequency(60);
        realtimeWriter.upsertAor(aor);

        if (sipPassword != null) {
            PsAuth auth = new PsAuth();
            auth.setId(authId);
            auth.setAuthType("userpass");
            auth.setUsername(id);
            auth.setPassword(sipPassword);
            realtimeWriter.upsertAuth(auth);
        }

        PsEndpoint ps = new PsEndpoint();
        ps.setId(id);
        ps.setTransport("transport-udp");
        ps.setAors(id);
        ps.setAuth(authId);
        ps.setContext(context);
        ps.setDisallow("all");
        ps.setAllow(endpoint.getCodecs());
        ps.setDirectMedia("no");
        ps.setRewriteContact("yes");
        ps.setForceRport("yes");
        ps.setRtpSymmetric("yes");
        ps.setCallerid(endpoint.getDisplayName() + " <" + endpoint.getExtension() + ">");
        ps.setSetVar("TENANT_ID=" + endpoint.getTenantId());
        realtimeWriter.upsertEndpoint(ps);

        dialplan.deleteAllByContextAndExten(context, realtimeExten);
        if (Boolean.TRUE.equals(endpoint.getEnabled())) {
            saveDialplan(context, realtimeExten, 1, "NoOp", "Calling endpoint " + id);
            saveDialplan(context, realtimeExten, 2, "Dial", "PJSIP/" + id + ",20");
            saveDialplan(context, realtimeExten, 3, "Hangup", "");
        }
    }

    public void renameOrDelete(Long tenantId, String oldExtension, String context) {
        String id = naming.endpoint(tenantId, oldExtension);
        queueMembers.deleteAllByStateInterface("PJSIP/" + id);
        endpoints.deleteById(id);
        auths.deleteById(naming.endpointAuth(tenantId, oldExtension));
        aors.deleteById(id);
        dialplan.deleteAllByContextAndExten(naming.tenantContext(tenantId), oldExtension);
        dialplan.deleteAllByContextAndExten("realtime", id);
    }

    private void saveDialplan(String context, String exten, int priority, String app, String appdata) {
        AsteriskExtension row = new AsteriskExtension();
        row.setContext(context);
        row.setExten(exten);
        row.setPriority(priority);
        row.setApp(app);
        row.setAppdata(appdata);
        dialplan.save(row);
    }
}
