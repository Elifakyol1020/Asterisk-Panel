package com.netgsm.asterisk.asterisk.realtime.repository;

import com.netgsm.asterisk.asterisk.realtime.entity.AsteriskQueueMember;
import com.netgsm.asterisk.asterisk.realtime.entity.AsteriskQueueMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsteriskQueueMemberRepository extends JpaRepository<AsteriskQueueMember, AsteriskQueueMemberId> {
    void deleteAllByQueueName(String queueName);
    void deleteAllByStateInterface(String stateInterface);
}
