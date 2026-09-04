package com.netgsm.asterisk.repository;
import com.netgsm.asterisk.entity.Dialplan;
import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DialplanRepository extends JpaRepository<Dialplan, Long> {
    Page<Dialplan> findAllByTenantId(Long tenantId, Pageable page);
    Optional<Dialplan> findByIdAndTenantId(Long id, Long tenantId);
    boolean existsByIdAndTenantId(Long id, Long tenantId);
    boolean existsByTenantIdAndExtensionAndPriority(Long tenantId, String extension, Integer priority);
    boolean existsByTenantIdAndExtensionAndPriorityAndIdNot(Long tenantId, String extension, Integer priority, Long id);
    boolean existsByTenantIdAndExtension(Long tenantId, String extension);
    List<Dialplan> findAllByTenantIdAndExtensionOrderByPriorityAsc(Long tenantId, String extension);
}
