package com.netgsm.asterisk.controller;

import com.netgsm.asterisk.service.PjsipGlobalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/pjsip-global")
@RequiredArgsConstructor
public class PjsipGlobalController {
    private final PjsipGlobalService service;
    @GetMapping public PjsipGlobalService.View get() { return service.get(); }
    @PutMapping public PjsipGlobalService.View save(@Valid @RequestBody PjsipGlobalService.Update request) { return service.save(request); }
}
