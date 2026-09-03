package com.netgsm.asterisk.controller;
import com.netgsm.asterisk.dto.CreateEndpointRequest;
import com.netgsm.asterisk.dto.EndpointResponse;
import com.netgsm.asterisk.dto.UpdateEndpointRequest;
import com.netgsm.asterisk.service.EndpointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/endpoints") @RequiredArgsConstructor
public class EndpointController {
    private final EndpointService service;
    @GetMapping public Page<EndpointResponse> list(@RequestParam(required = false) Long tenantId, Pageable page) { return service.list(tenantId, page); }
    @GetMapping("/{id}") public EndpointResponse get(@PathVariable Long id) { return service.get(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public EndpointResponse create(@Valid @RequestBody CreateEndpointRequest request) { return service.create(request); }
    @PutMapping("/{id}")
    public EndpointResponse update(@PathVariable Long id, @Valid @RequestBody UpdateEndpointRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
