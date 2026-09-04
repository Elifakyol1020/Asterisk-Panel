package com.netgsm.asterisk.repository;
import com.netgsm.asterisk.entity.IvrOption;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface IvrOptionRepository extends JpaRepository<IvrOption, Long> {
    Page<IvrOption> findAllByIvrIdAndTenantId(Long ivrId, Long tenantId, Pageable page);
    List<IvrOption> findAllByIvrIdAndTenantIdOrderByDigitAsc(Long ivrId, Long tenantId);
    Optional<IvrOption> findByIdAndIvrIdAndTenantId(Long id, Long ivrId, Long tenantId);
    boolean existsByIvrIdAndDigit(Long ivrId, String digit);
    boolean existsByIvrIdAndDigitAndIdNot(Long ivrId, String digit, Long id);
    boolean existsByIvrId(Long ivrId);
    boolean existsByTenantIdAndIvrId(Long tenantId, Long ivrId);
    boolean existsByActionTypeAndTargetId(String actionType, Long targetId);
    boolean existsByTenantIdAndActionTypeAndTargetId(Long tenantId, String actionType, Long targetId);
}
