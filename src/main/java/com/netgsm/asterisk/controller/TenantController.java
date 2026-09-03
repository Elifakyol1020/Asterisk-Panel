package com.netgsm.asterisk.controller;
import com.netgsm.asterisk.dto.TenantRequest;
import com.netgsm.asterisk.dto.TenantResponse;
import com.netgsm.asterisk.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/admin/tenants") @RequiredArgsConstructor
public class TenantController {
    private final TenantService service;
    @GetMapping public Page<TenantResponse> list(Pageable page) { return service.list(page); }
    @GetMapping("/{id}") public TenantResponse get(@PathVariable Long id) { return service.get(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public TenantResponse create(@Valid @RequestBody TenantRequest request) { return service.create(request); }
    @PutMapping("/{id}")
    public TenantResponse update(@PathVariable Long id, @Valid @RequestBody TenantRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
