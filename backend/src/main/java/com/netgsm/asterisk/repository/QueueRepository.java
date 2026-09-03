package com.netgsm.asterisk.repository;
import com.netgsm.asterisk.entity.Queue;
import java.util.Optional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface QueueRepository extends JpaRepository<Queue, Long> {
    Page<Queue> findAllByTenantId(Long tenantId, Pageable page);
    Optional<Queue> findByIdAndTenantId(Long id, Long tenantId);
    boolean existsByIdAndTenantId(Long id, Long tenantId);
    boolean existsByTenantIdAndName(Long tenantId, String name);
    boolean existsByTenantIdAndNameAndIdNot(Long tenantId, String name, Long id);
}
