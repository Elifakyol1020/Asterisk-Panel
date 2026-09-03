package com.netgsm.asterisk.entity;
import com.netgsm.asterisk.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter @Entity @Table(name = "ivr_options", schema = "platform",
        uniqueConstraints = @UniqueConstraint(columnNames = {"ivr_id", "digit"}))
public class IvrOption extends TenantEntity {
    @Column(nullable = false) private Long ivrId;
    @Column(nullable = false) private String digit;
    @Column(nullable = false) private String actionType;
    private Long targetId;
}
