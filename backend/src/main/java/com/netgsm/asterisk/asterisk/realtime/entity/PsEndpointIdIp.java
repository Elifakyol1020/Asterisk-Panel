package com.netgsm.asterisk.asterisk.realtime.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity @Table(name = "ps_endpoint_id_ips", schema = "public")
public class PsEndpointIdIp {
    @Id
    @Column(length = 255)
    private String id;
    @Column(length = 255)
    private String endpoint;
    @Column(name = "match", length = 80)
    private String matchValue;
}
