package com.netgsm.asterisk.controller;
import com.netgsm.asterisk.dto.request.CdrSearchRequest;
import com.netgsm.asterisk.dto.response.CdrResponse;
import com.netgsm.asterisk.service.CdrService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/cdr") @RequiredArgsConstructor
public class CdrController {
    private final CdrService service;
    @GetMapping public Page<CdrResponse> list(@Valid @ModelAttribute CdrSearchRequest request) { return service.list(request); }
    @GetMapping("/{id}") public CdrResponse get(@PathVariable String id) { return service.get(id); }
}
