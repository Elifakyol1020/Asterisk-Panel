package com.netgsm.asterisk.service.provisioning;

import com.netgsm.asterisk.repository.DialplanRepository;
import com.netgsm.asterisk.repository.EndpointRepository;
import com.netgsm.asterisk.repository.ExtensionRepository;
import com.netgsm.asterisk.repository.IvrRepository;
import com.netgsm.asterisk.repository.QueueMemberRepository;
import com.netgsm.asterisk.repository.QueueRepository;
import com.netgsm.asterisk.repository.TrunkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Rebuilds application-owned Realtime rows from the platform source of truth. */
@Component
@Order(100)
@RequiredArgsConstructor
@Slf4j
public class AsteriskProvisioningReconciler implements ApplicationRunner {
    private final EndpointRepository endpoints;
    private final TrunkRepository trunks;
    private final QueueRepository queues;
    private final QueueMemberRepository queueMembers;
    private final IvrRepository ivrs;
    private final ExtensionRepository extensions;
    private final DialplanRepository dialplans;
    private final AsteriskEndpointProvisioningService endpointProvisioning;
    private final AsteriskTrunkProvisioningService trunkProvisioning;
    private final AsteriskQueueProvisioningService queueProvisioning;
    private final AsteriskDialplanProvisioningService dialplanProvisioning;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        endpoints.findAll().forEach(endpoint -> endpointProvisioning.upsert(endpoint, null));
        trunks.findAll().forEach(trunk -> trunkProvisioning.upsert(trunk, null));
        queues.findAll().forEach(queueProvisioning::upsertQueue);
        queueMembers.findAll().forEach(member -> {
            var queue = queues.findByIdAndTenantId(member.getQueueId(), member.getTenantId()).orElseThrow();
            var endpoint = endpoints.findByIdAndTenantId(member.getEndpointId(), member.getTenantId()).orElseThrow();
            queueProvisioning.upsertMember(queue, member, endpoint);
        });
        ivrs.findAll().forEach(dialplanProvisioning::recompileIvr);
        extensions.findAll().forEach(dialplanProvisioning::upsertExtensionRoute);
        dialplans.findAll().forEach(dialplanProvisioning::upsertDialplan);
        log.info("Asterisk Realtime provisioning reconciled with tenant-specific contexts");
    }
}
