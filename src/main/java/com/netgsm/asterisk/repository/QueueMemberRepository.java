package com.netgsm.asterisk.repository;
import com.netgsm.asterisk.entity.QueueMember;
import java.util.Optional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface QueueMemberRepository extends JpaRepository<QueueMember, Long> {
    Page<QueueMember> findAllByQueueIdAndTenantId(Long queueId, Long tenantId, Pageable page);
    Optional<QueueMember> findByIdAndQueueIdAndTenantId(Long id, Long queueId, Long tenantId);
    boolean existsByQueueIdAndEndpointId(Long queueId, Long endpointId);
    boolean existsByEndpointId(Long endpointId);
    boolean existsByQueueId(Long queueId);
}
