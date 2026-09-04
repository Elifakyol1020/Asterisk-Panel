package com.netgsm.asterisk.asterisk.realtime.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity @Table(name = "ps_endpoints", schema = "public")
public class PsEndpoint {
    @Id
    @Column(length = 255)
    private String id;
    @Column(length = 40)
    private String transport;
    @Column(length = 2048)
    private String aors;
    @Column(length = 255)
    private String auth;
    @Column(name = "outbound_auth", length = 255)
    private String outboundAuth;
    @Column(length = 40)
    private String context;
    @Column(length = 200)
    private String disallow;
    @Column(length = 200)
    private String allow;
    @Column(name = "direct_media")
    private String directMedia;
    @Column(name = "force_rport")
    private String forceRport;
    @Column(name = "rewrite_contact")
    private String rewriteContact;
    @Column(name = "rtp_symmetric")
    private String rtpSymmetric;
    @Column(name = "from_user", length = 40)
    private String fromUser;
    @Column(name = "from_domain", length = 40)
    private String fromDomain;
    @Column(length = 40)
    private String callerid;
    @Column(name = "set_var")
    private String setVar;
}
