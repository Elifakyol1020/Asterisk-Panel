package com.netgsm.asterisk.service.provisioning;

import com.netgsm.asterisk.asterisk.realtime.entity.AsteriskQueue;
import com.netgsm.asterisk.asterisk.realtime.entity.AsteriskQueueMember;
import com.netgsm.asterisk.asterisk.realtime.repository.AsteriskQueueMemberRepository;
import com.netgsm.asterisk.asterisk.realtime.repository.AsteriskQueueRepository;
import com.netgsm.asterisk.entity.Endpoint;
import com.netgsm.asterisk.entity.Queue;
import com.netgsm.asterisk.entity.QueueMember;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsteriskQueueProvisioningService {
    private final AsteriskNaming naming;
    private final AsteriskQueueRepository queues;
    private final AsteriskQueueMemberRepository members;
    private final AsteriskRealtimeWriter realtimeWriter;

    public void upsertQueue(Queue queue) {
        String name = naming.queue(queue.getTenantId(), queue.getName());
        log.info("Creating Asterisk queue: {}", name);
        AsteriskQueue realtime = new AsteriskQueue();
        realtime.setName(name);
        realtime.setMusicOnHold(queue.getMusicOnHold());
        realtime.setContext(naming.routerContext());
        realtime.setTimeout(queue.getTimeout());
        realtime.setRetry(queue.getRetry());
        realtime.setWrapuptime(queue.getWrapupTime());
        realtime.setMaxlen(queue.getMaxLength());
        realtime.setStrategy(queue.getStrategy());
        realtime.setRinginuse("no");
        realtime.setAutofill("yes");
        realtime.setAnnounceFrequency(60);
        realtime.setAnnouncePosition("yes");
        realtime.setPeriodicAnnounceFrequency(60);
        realtime.setJoinempty("yes");
        realtime.setLeavewhenempty("no");
        realtime.setMonitorFormat("wav");
        realtimeWriter.upsertQueue(realtime);
    }

    public void renameOrDeleteQueue(Long tenantId, String oldName) {
        String queueName = naming.queue(tenantId, oldName);
        members.deleteAllByQueueName(queueName);
        queues.deleteById(queueName);
    }

    public void upsertMember(Queue queue, QueueMember member, Endpoint endpoint) {
        String queueName = naming.queue(queue.getTenantId(), queue.getName());
        String endpointId = naming.endpoint(endpoint.getTenantId(), endpoint.getExtension());
        String iface = "PJSIP/" + endpointId;
        log.info("Creating Asterisk queue member: {} -> {}", queueName, iface);
        AsteriskQueueMember realtime = new AsteriskQueueMember();
        realtime.setQueueName(queueName);
        realtime.setInterfaceName(iface);
        realtime.setMembername(endpoint.getExtension());
        realtime.setStateInterface(iface);
        realtime.setPenalty(member.getPenalty());
        realtime.setPaused(Boolean.TRUE.equals(member.getPaused()) ? 1 : 0);
        realtime.setUniqueid(Math.toIntExact(member.getId()));
        realtime.setWrapuptime(queue.getWrapupTime());
        realtime.setRinginuse("no");
        realtimeWriter.upsertQueueMember(realtime);
    }

    public void deleteMember(Queue queue, Endpoint endpoint) {
        String queueName = naming.queue(queue.getTenantId(), queue.getName());
        String iface = "PJSIP/" + naming.endpoint(endpoint.getTenantId(), endpoint.getExtension());
        members.deleteById(new com.netgsm.asterisk.asterisk.realtime.entity.AsteriskQueueMemberId(queueName, iface));
    }
}
