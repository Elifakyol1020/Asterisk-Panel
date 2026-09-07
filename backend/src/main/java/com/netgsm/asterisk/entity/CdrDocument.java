package com.netgsm.asterisk.entity;
import java.time.Instant;
import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "cdr", schema = "platform")
public class CdrDocument {
    @Id @Column(length = 64) private String id;
    private Long tenantId;
    @Column(name = "cdr_sequence", nullable = false) private Integer sequence;
    @Column(length = 150, nullable = false) private String uniqueId;
    @Column(length = 150) private String linkedId;
    private String src;
    private String dst;
    private String srcName;
    private String dstName;
    private String context;
    private String channel;
    private String dstChannel;
    private Instant startTime;
    private Instant answerTime;
    private Instant endTime;
    private Integer duration;
    private Integer billsec;
    @Column(length = 32) private String disposition;
    @Column(length = 1000) private String recordingPath;
}
