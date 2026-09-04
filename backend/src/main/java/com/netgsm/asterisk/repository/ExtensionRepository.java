package com.netgsm.asterisk.repository;
import com.netgsm.asterisk.entity.Extension;
import java.util.Optional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ExtensionRepository extends JpaRepository<Extension, Long> {
    boolean existsByTargetTypeAndTargetId(String targetType, Long targetId);
    boolean existsByTenantIdAndTargetTypeAndTargetId(Long tenantId, String targetType, Long targetId);
    Page<Extension> findAllByTenantId(Long tenantId, Pageable page);
    Optional<Extension> findByIdAndTenantId(Long id, Long tenantId);
    boolean existsByIdAndTenantId(Long id, Long tenantId);
    boolean existsByTenantIdAndExtensionNumber(Long tenantId, String extensionNumber);
    boolean existsByTenantIdAndExtensionNumberAndIdNot(Long tenantId, String extensionNumber, Long id);
}
