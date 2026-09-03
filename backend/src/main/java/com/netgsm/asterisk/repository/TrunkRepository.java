package com.netgsm.asterisk.repository;
import com.netgsm.asterisk.entity.Trunk;
import java.util.Optional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface TrunkRepository extends JpaRepository<Trunk, Long> {
    Page<Trunk> findAllByTenantId(Long tenantId, Pageable page);
    Optional<Trunk> findByIdAndTenantId(Long id, Long tenantId);
    boolean existsByIdAndTenantId(Long id, Long tenantId);
    boolean existsByTenantIdAndName(Long tenantId, String name);
    boolean existsByTenantIdAndNameAndIdNot(Long tenantId, String name, Long id);
}
