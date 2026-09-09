package com.netgsm.asterisk.entity;
import java.time.Instant;
import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "cdr", schema = "platform")
public class CdrDocument {
    @Id @Column(length = 64) private String id;
    // Computed by the backend query, never trusted from a stored tenant_id value.
    @org.hibernate.annotations.Formula("""
        (select t.id from platform.tenants t where
          (coalesce(regexp_like(trim(context), concat('^tenant_', cast(t.id as varchar), '_.+$')), false)
           or coalesce(regexp_like(trim(channel), concat('^(PJSIP|SIP)/tenant', cast(t.id as varchar), '_.+$')), false)
           or coalesce(regexp_like(trim(dst_channel), concat('^(PJSIP|SIP)/tenant', cast(t.id as varchar), '_.+$')), false)
           or coalesce(regexp_like(trim(channel), concat('^(PJSIP|SIP)/[0-9]{1,20}-', t.code, '(-[a-zA-Z0-9]+)?$')), false)
           or coalesce(regexp_like(trim(dst_channel), concat('^(PJSIP|SIP)/[0-9]{1,20}-', t.code, '(-[a-zA-Z0-9]+)?$')), false))
          and (not coalesce(regexp_like(trim(context), '^tenant_([1-9][0-9]*)_.+$'), false)
               or regexp_like(trim(context), concat('^tenant_', cast(t.id as varchar), '_.+$')))
          and (not coalesce(regexp_like(trim(channel), '^(PJSIP|SIP)/tenant([1-9][0-9]*)_.+$'), false)
               or regexp_like(trim(channel), concat('^(PJSIP|SIP)/tenant', cast(t.id as varchar), '_.+$')))
          and (not coalesce(regexp_like(trim(dst_channel), '^(PJSIP|SIP)/tenant([1-9][0-9]*)_.+$'), false)
               or regexp_like(trim(dst_channel), concat('^(PJSIP|SIP)/tenant', cast(t.id as varchar), '_.+$')))
          and (not coalesce(regexp_like(trim(channel), '^(PJSIP|SIP)/[0-9]{1,20}-[0-9]{4,6}(-[a-zA-Z0-9]+)?$'), false)
               or regexp_like(trim(channel), concat('^(PJSIP|SIP)/[0-9]{1,20}-', t.code, '(-[a-zA-Z0-9]+)?$')))
          and (not coalesce(regexp_like(trim(dst_channel), '^(PJSIP|SIP)/[0-9]{1,20}-[0-9]{4,6}(-[a-zA-Z0-9]+)?$'), false)
               or regexp_like(trim(dst_channel), concat('^(PJSIP|SIP)/[0-9]{1,20}-', t.code, '(-[a-zA-Z0-9]+)?$'))))
        """)
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
