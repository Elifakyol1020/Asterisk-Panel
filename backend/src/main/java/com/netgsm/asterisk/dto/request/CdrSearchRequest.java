package com.netgsm.asterisk.dto.request;
import java.time.Instant;
import lombok.Data;
import jakarta.validation.constraints.*;
@Data
public class CdrSearchRequest {
    @Min(0) private int page = 0;
    @Min(1) @Max(100) private int size = 20;
    @Size(max=255) private String src;
    @Size(max=255) private String dst;
    @Pattern(regexp="ANSWERED|NO ANSWER|BUSY|FAILED|CONGESTION") private String disposition;
    private Instant startDate;
    private Instant endDate;
    @Min(0) private Integer minDuration;
    @Min(0) private Integer maxDuration;
    @Size(max=150) private String uniqueId;
    @Size(max=150) private String linkedId;
    private boolean prefix = false;
    @Positive private Long tenantId;
    public void validateRange() {
        if ((long) page * size + size > 10000)
            throw new IllegalArgumentException("Narrow date range: maximum result window is 10000");
        if (startDate != null && endDate != null && !startDate.isBefore(endDate))
            throw new IllegalArgumentException("Invalid date range");
        if (minDuration != null && maxDuration != null && minDuration > maxDuration)
            throw new IllegalArgumentException("Invalid duration range");
    }
}
