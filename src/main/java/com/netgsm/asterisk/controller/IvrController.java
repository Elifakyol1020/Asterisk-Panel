package com.netgsm.asterisk.controller;
import com.netgsm.asterisk.dto.CreateIvrRequest;
import com.netgsm.asterisk.dto.IvrOptionRequest;
import com.netgsm.asterisk.dto.IvrOptionResponse;
import com.netgsm.asterisk.dto.IvrResponse;
import com.netgsm.asterisk.dto.UpdateIvrRequest;
import com.netgsm.asterisk.service.IvrService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/ivrs") @RequiredArgsConstructor
public class IvrController {
    private final IvrService service;
    @GetMapping public Page<IvrResponse> list(@RequestParam(required = false) Long tenantId, Pageable page) { return service.list(tenantId, page); }
    @GetMapping("/{id}") public IvrResponse get(@PathVariable Long id) { return service.get(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public IvrResponse create(@Valid @RequestBody CreateIvrRequest request) { return service.create(request); }
    @PutMapping("/{id}")
    public IvrResponse update(@PathVariable Long id, @Valid @RequestBody UpdateIvrRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
