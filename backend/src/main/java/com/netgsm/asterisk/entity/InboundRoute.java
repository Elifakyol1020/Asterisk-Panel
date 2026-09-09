package com.netgsm.asterisk.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter @Entity
@Table(name="inbound_routes", schema="platform", uniqueConstraints=@UniqueConstraint(columnNames={"tenant_id","trunk_id","did"}))
public class InboundRoute extends TenantEntity {
 @Column(nullable=false) private String name;
 @Column(nullable=false) private Long trunkId;
 @Column(nullable=false) private String did;
 @Column(nullable=false) private String targetType;
 @Column(nullable=false) private Long targetId;
 @Column(nullable=false) private Boolean enabled;
}
