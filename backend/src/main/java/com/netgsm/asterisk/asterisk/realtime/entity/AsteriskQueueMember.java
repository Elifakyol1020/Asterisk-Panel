package com.netgsm.asterisk.asterisk.realtime.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity @IdClass(AsteriskQueueMemberId.class) @Table(name = "queue_members", schema = "public")
public class AsteriskQueueMember {
    @Id
    @Column(name = "queue_name", length = 80)
    private String queueName;
    @Id
    @Column(name = "interface", length = 80)
    private String interfaceName;
    @Column(length = 80)
    private String membername;
    @Column(name = "state_interface", length = 80)
    private String stateInterface;
    private Integer penalty;
    private Integer paused;
    private Integer uniqueid;
    private Integer wrapuptime;
    private String ringinuse;
}
