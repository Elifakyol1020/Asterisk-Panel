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
        backfillFlowExtensions();
        endpoints.findAll().forEach(endpoint -> endpointProvisioning.upsert(endpoint, null));
        trunks.findAll().forEach(trunk -> trunkProvisioning.upsert(trunk, null));
        queues.findAll().forEach(queueProvisioning::upsertQueue);
        queueMembers.findAll().forEach(member -> {
            var queue = queues.findByIdAndTenantId(member.getQueueId(), member.getTenantId()).orElseThrow();
            var endpoint = endpoints.findByIdAndTenantId(member.getEndpointId(), member.getTenantId()).orElseThrow();
            queueProvisioning.upsertMember(queue, member, endpoint);
        });
        ivrs.findAll().forEach(dialplanProvisioning::recompileIvr);
        extensions.findAll().stream()
                .filter(extension -> !"DIALPLAN".equals(extension.getTargetType()))
                .forEach(dialplanProvisioning::upsertExtensionRoute);
        dialplans.findAll().forEach(dialplanProvisioning::upsertDialplan);
        log.info("Asterisk Realtime provisioning reconciled with tenant-specific contexts");
    }

    private void backfillFlowExtensions() {
        dialplans.findAll().stream()
                .collect(java.util.stream.Collectors.groupingBy(row -> row.getTenantId() + ":" + row.getExtension()))
                .values().forEach(rows -> {
                    var first = rows.stream().min(java.util.Comparator.comparing(com.netgsm.asterisk.entity.Dialplan::getPriority)).orElseThrow();
                    if (extensions.existsByTenantIdAndExtensionNumber(first.getTenantId(), first.getExtension())) return;
                    var extension = new com.netgsm.asterisk.entity.Extension();
                    extension.setTenantId(first.getTenantId());
                    extension.setExtensionNumber(first.getExtension());
                    extension.setName("Gelişmiş akış " + first.getExtension());
                    extension.setTargetType("DIALPLAN");
                    extension.setTargetId(first.getId());
                    extension.setEnabled(rows.stream().anyMatch(row -> Boolean.TRUE.equals(row.getEnabled())));
                    extension.setContext(first.getContext());
                    extensions.save(extension);
                });
        extensions.flush();
    }
}
