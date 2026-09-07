package com.netgsm.asterisk.entity;
import java.time.Instant;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
@Data
@Document(indexName = "#{@cdrIndex.name}", createIndex = false)
public class CdrDocument {
    @Id private String id;
    @Field(type = FieldType.Long) private Long tenantId;
    @Field(type = FieldType.Keyword) private String sortId;
    @Field(type = FieldType.Integer) private Integer sequence;
    @Field(type = FieldType.Keyword)
    private String uniqueId;
    @Field(type = FieldType.Keyword)
    private String linkedId;
    @Field(type = FieldType.Keyword)
    private String src;
    @Field(type = FieldType.Keyword)
    private String dst;
    @Field(type = FieldType.Keyword)
    private String srcName;
    @Field(type = FieldType.Keyword)
    private String dstName;
    @Field(type = FieldType.Keyword)
    private String context;
    @Field(type = FieldType.Keyword)
    private String channel;
    @Field(type = FieldType.Keyword)
    private String dstChannel;
    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant startTime;
    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant answerTime;
    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant endTime;
    @Field(type = FieldType.Integer)
    private Integer duration;
    @Field(type = FieldType.Integer)
    private Integer billsec;
    @Field(type = FieldType.Keyword)
    private String disposition;
    @Field(type = FieldType.Keyword)
    private String recordingPath;
}
