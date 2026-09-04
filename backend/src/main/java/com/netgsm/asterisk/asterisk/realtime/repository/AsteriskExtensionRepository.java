package com.netgsm.asterisk.asterisk.realtime.repository;

import com.netgsm.asterisk.asterisk.realtime.entity.AsteriskExtension;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsteriskExtensionRepository extends JpaRepository<AsteriskExtension, Long> {
    void deleteAllByContextAndExten(String context, String exten);
    void deleteAllByContextAndExtenAndPriority(String context, String exten, Integer priority);
    void deleteAllByContext(String context);
    void deleteAllByContextAndExtenStartingWith(String context, String extenPrefix);
    List<AsteriskExtension> findAllByContextAndExtenOrderByPriorityAsc(String context, String exten);
}
