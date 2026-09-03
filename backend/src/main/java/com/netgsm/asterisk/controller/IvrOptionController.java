package com.netgsm.asterisk.controller;
import com.netgsm.asterisk.dto.CreateIvrRequest;
import com.netgsm.asterisk.dto.IvrOptionRequest;
import com.netgsm.asterisk.dto.IvrOptionResponse;
import com.netgsm.asterisk.dto.IvrResponse;
import com.netgsm.asterisk.dto.UpdateIvrRequest;
import com.netgsm.asterisk.service.IvrOptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/ivrs/{ivrId}/options") @RequiredArgsConstructor
public class IvrOptionController {
    private final IvrOptionService service;
    @io.swagger.v3.oas.annotations.Parameter(name = "sort", description = "İsteğe bağlı. Örnek: id,desc. JSON köşeli parantezleri ve tırnak kullanmayın; sıralama istemiyorsanız boş bırakın.")
    @GetMapping public Page<IvrOptionResponse> list(@PathVariable Long ivrId, @ParameterObject Pageable page) { return service.list(ivrId, page); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public IvrOptionResponse create(@PathVariable Long ivrId, @Valid @RequestBody IvrOptionRequest request) { return service.create(ivrId, request); }
    @PutMapping("/{id}") public IvrOptionResponse update(@PathVariable Long ivrId, @PathVariable Long id, @Valid @RequestBody IvrOptionRequest request) { return service.update(ivrId, id, request); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long ivrId, @PathVariable Long id) { service.delete(ivrId, id); }
}
