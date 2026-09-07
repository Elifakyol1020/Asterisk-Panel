package com.netgsm.asterisk.controller;
import com.netgsm.asterisk.dto.request.CdrInput;
import com.netgsm.asterisk.dto.response.CdrResponse;
import com.netgsm.asterisk.service.CdrIngestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/admin/cdr") @RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class CdrIngestionController {
    private final CdrIngestionService ingestion;
    @PostMapping public CdrResponse ingest(@Valid @RequestBody CdrInput input) { return ingestion.ingest(input); }
}
