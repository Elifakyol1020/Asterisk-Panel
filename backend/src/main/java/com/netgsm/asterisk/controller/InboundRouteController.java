package com.netgsm.asterisk.controller;
import com.netgsm.asterisk.dto.response.InboundRouteResponse;
import com.netgsm.asterisk.dto.request.InboundRouteRequest;
import com.netgsm.asterisk.service.InboundRouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/inbound-routes") @RequiredArgsConstructor
public class InboundRouteController {
    private final InboundRouteService service;
    @io.swagger.v3.oas.annotations.Parameter(name = "sort", description = "İsteğe bağlı. Örnek: id,desc. JSON köşeli parantezleri ve tırnak kullanmayın; sıralama istemiyorsanız boş bırakın.")
    @GetMapping public Page<InboundRouteResponse> list(@RequestParam(required = false) Long tenantId, @ParameterObject Pageable page) { return service.list(tenantId, page); }
    @GetMapping("/{id}") public InboundRouteResponse get(@PathVariable Long id) { return service.get(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public InboundRouteResponse create(@Valid @RequestBody InboundRouteRequest request) { return service.create(request); }
    @PutMapping("/{id}")
    public InboundRouteResponse update(@PathVariable Long id, @Valid @RequestBody InboundRouteRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
