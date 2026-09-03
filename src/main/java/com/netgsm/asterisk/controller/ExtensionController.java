package com.netgsm.asterisk.controller;
import com.netgsm.asterisk.dto.CreateExtensionRequest;
import com.netgsm.asterisk.dto.ExtensionResponse;
import com.netgsm.asterisk.dto.UpdateExtensionRequest;
import com.netgsm.asterisk.service.ExtensionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/extensions") @RequiredArgsConstructor
public class ExtensionController {
    private final ExtensionService service;
    @GetMapping public Page<ExtensionResponse> list(@RequestParam(required = false) Long tenantId, Pageable page) { return service.list(tenantId, page); }
    @GetMapping("/{id}") public ExtensionResponse get(@PathVariable Long id) { return service.get(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public ExtensionResponse create(@Valid @RequestBody CreateExtensionRequest request) { return service.create(request); }
    @PutMapping("/{id}")
    public ExtensionResponse update(@PathVariable Long id, @Valid @RequestBody UpdateExtensionRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
