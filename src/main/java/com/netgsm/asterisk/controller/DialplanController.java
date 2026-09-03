package com.netgsm.asterisk.controller;
import com.netgsm.asterisk.dto.CreateDialplanRequest;
import com.netgsm.asterisk.dto.DialplanResponse;
import com.netgsm.asterisk.dto.UpdateDialplanRequest;
import com.netgsm.asterisk.service.DialplanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/dialplans") @RequiredArgsConstructor
public class DialplanController {
    private final DialplanService service;
    @GetMapping public Page<DialplanResponse> list(@RequestParam(required = false) Long tenantId, Pageable page) { return service.list(tenantId, page); }
    @GetMapping("/{id}") public DialplanResponse get(@PathVariable Long id) { return service.get(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public DialplanResponse create(@Valid @RequestBody CreateDialplanRequest request) { return service.create(request); }
    @PutMapping("/{id}")
    public DialplanResponse update(@PathVariable Long id, @Valid @RequestBody UpdateDialplanRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
