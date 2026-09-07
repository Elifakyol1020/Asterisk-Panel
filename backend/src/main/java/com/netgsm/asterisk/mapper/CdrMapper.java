package com.netgsm.asterisk.mapper;
import com.netgsm.asterisk.entity.CdrDocument;
import com.netgsm.asterisk.dto.request.CdrInput;
import com.netgsm.asterisk.dto.response.CdrResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;
import org.springframework.stereotype.Component;
@Component
public class CdrMapper {
    public CdrDocument toDocument(CdrInput input, Long tenantId) {
        var document = new CdrDocument();
        document.setTenantId(tenantId);
        document.setSequence(input.sequence());
        document.setUniqueId(input.uniqueId());
        document.setLinkedId(input.linkedId());
        document.setSrc(input.src());
        document.setDst(input.dst());
        document.setSrcName(input.srcName());
        document.setDstName(input.dstName());
        document.setContext(input.context());
        document.setChannel(input.channel());
        document.setDstChannel(input.dstChannel());
        document.setStartTime(input.startTime());
        document.setAnswerTime(input.answerTime());
        document.setEndTime(input.endTime());
        document.setDuration(input.duration());
        document.setBillsec(input.billsec());
        document.setDisposition(input.disposition());
        document.setRecordingPath(input.recordingPath());
        document.setDisposition(input.disposition().trim().toUpperCase(Locale.ROOT));
        document.setSrc(input.src().trim());
        document.setDst(input.dst().trim());
        document.setUniqueId(input.uniqueId().trim());
        try {
            String identity = tenantId + ":" + document.getUniqueId() + ":" + input.sequence();
            document.setId(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(identity.getBytes(StandardCharsets.UTF_8))));
        } catch (java.security.NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
        return document;
    }
    public CdrResponse toResponse(CdrDocument d) {
        return new CdrResponse(d.getId(), d.getTenantId(), d.getSequence(),
            d.getUniqueId(),
            d.getLinkedId(),
            d.getSrc(),
            d.getDst(),
            d.getSrcName(),
            d.getDstName(),
            d.getContext(),
            d.getChannel(),
            d.getDstChannel(),
            d.getStartTime(),
            d.getAnswerTime() != null && !d.getAnswerTime().isAfter(java.time.Instant.EPOCH) ? null : d.getAnswerTime(),
            d.getEndTime(),
            d.getDuration(),
            d.getBillsec(),
            d.getDisposition(),
            d.getRecordingPath());
    }
}
