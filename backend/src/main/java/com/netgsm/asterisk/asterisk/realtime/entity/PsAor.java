package com.netgsm.asterisk.asterisk.realtime.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity @Table(name = "ps_aors", schema = "public")
public class PsAor {
    @Id
    @Column(length = 255)
    private String id;
    @Column(length = 255)
    private String contact;
    @Column(name = "max_contacts")
    private Integer maxContacts;
    @Column(name = "remove_existing")
    private String removeExisting;
    @Column(name = "qualify_frequency")
    private Integer qualifyFrequency;
}
