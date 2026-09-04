package com.netgsm.asterisk.asterisk.realtime.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity @Table(name = "queues", schema = "public")
public class AsteriskQueue {
    @Id
    @Column(length = 128)
    private String name;
    @Column(name = "musiconhold", length = 128)
    private String musicOnHold;
    @Column(length = 128)
    private String context;
    private Integer timeout;
    private Integer retry;
    private Integer wrapuptime;
    private Integer maxlen;
    private String strategy;
    private String ringinuse;
    private String autofill;
    @Column(name = "announce_frequency")
    private Integer announceFrequency;
    @Column(name = "announce_position", length = 128)
    private String announcePosition;
    @Column(name = "periodic_announce_frequency")
    private Integer periodicAnnounceFrequency;
    @Column(length = 128)
    private String joinempty;
    @Column(length = 128)
    private String leavewhenempty;
    @Column(name = "monitor_format", length = 8)
    private String monitorFormat;
}
