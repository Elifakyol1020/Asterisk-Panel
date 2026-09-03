package com.netgsm.asterisk.repository;
import com.netgsm.asterisk.entity.User;
import java.util.Optional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByRole(com.netgsm.asterisk.enums.Role role);
    Optional<User> findByEmail(String email);
    Page<User> findAllByTenantId(Long tenantId, Pageable pageable);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsernameAndIdNot(String username, Long id);
    boolean existsByEmailAndIdNot(String email, Long id);
}
