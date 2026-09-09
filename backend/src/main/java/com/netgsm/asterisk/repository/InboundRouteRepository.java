package com.netgsm.asterisk.repository;
import com.netgsm.asterisk.entity.InboundRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.*;
import java.util.Optional;
public interface InboundRouteRepository extends JpaRepository<InboundRoute,Long> {
 java.util.List<InboundRoute> findAllByTenantIdAndTargetTypeAndTargetId(Long tenantId,String targetType,Long targetId);
 Page<InboundRoute> findAllByTenantId(Long tenantId, Pageable page);
 Optional<InboundRoute> findByIdAndTenantId(Long id,Long tenantId);
 boolean existsByTenantIdAndTrunkIdAndDidAndIdNot(Long tenantId, Long trunkId, String did, Long id);
 boolean existsByTenantIdAndTrunkId(Long tenantId, Long trunkId);
 boolean existsByTenantIdAndTargetTypeAndTargetId(Long tenantId,String targetType,Long targetId);
}
