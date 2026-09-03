package com.netgsm.asterisk.controller;
import com.netgsm.asterisk.dto.CreateTrunkRequest;
import com.netgsm.asterisk.dto.TrunkResponse;
import com.netgsm.asterisk.dto.UpdateTrunkRequest;
import com.netgsm.asterisk.service.TrunkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/trunks") @RequiredArgsConstructor
public class TrunkController {
    private final TrunkService service;
    @io.swagger.v3.oas.annotations.Parameter(name = "sort", description = "İsteğe bağlı. Örnek: id,desc. JSON köşeli parantezleri ve tırnak kullanmayın; sıralama istemiyorsanız boş bırakın.")
    @GetMapping public Page<TrunkResponse> list(@RequestParam(required = false) Long tenantId, @ParameterObject Pageable page) { return service.list(tenantId, page); }
    @GetMapping("/{id}") public TrunkResponse get(@PathVariable Long id) { return service.get(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public TrunkResponse create(@Valid @RequestBody CreateTrunkRequest request) { return service.create(request); }
    @PutMapping("/{id}")
    public TrunkResponse update(@PathVariable Long id, @Valid @RequestBody UpdateTrunkRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
