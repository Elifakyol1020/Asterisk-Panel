package com.netgsm.asterisk.repository;
import com.netgsm.asterisk.entity.Endpoint;
import java.util.Optional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface EndpointRepository extends JpaRepository<Endpoint, Long> {
    Page<Endpoint> findAllByTenantId(Long tenantId, Pageable page);
    Optional<Endpoint> findByIdAndTenantId(Long id, Long tenantId);
    boolean existsByIdAndTenantId(Long id, Long tenantId);
    boolean existsByTenantIdAndExtension(Long tenantId, String extension);
    boolean existsByTenantIdAndExtensionAndIdNot(Long tenantId, String extension, Long id);
}
