package com.netgsm.asterisk.repository;
import com.netgsm.asterisk.entity.Ivr;
import java.util.Optional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface IvrRepository extends JpaRepository<Ivr, Long> {
    Page<Ivr> findAllByTenantId(Long tenantId, Pageable page);
    Optional<Ivr> findByIdAndTenantId(Long id, Long tenantId);
    boolean existsByIdAndTenantId(Long id, Long tenantId);
    boolean existsByTenantIdAndName(Long tenantId, String name);
    boolean existsByTenantIdAndNameAndIdNot(Long tenantId, String name, Long id);
}
