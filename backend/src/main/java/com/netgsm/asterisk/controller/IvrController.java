package com.netgsm.asterisk.controller;
import com.netgsm.asterisk.dto.request.CreateIvrRequest;
import com.netgsm.asterisk.dto.request.IvrOptionRequest;
import com.netgsm.asterisk.dto.response.IvrOptionResponse;
import com.netgsm.asterisk.dto.response.IvrResponse;
import com.netgsm.asterisk.dto.request.UpdateIvrRequest;
import com.netgsm.asterisk.service.IvrService;
import com.netgsm.asterisk.service.IvrAudioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/ivrs") @RequiredArgsConstructor
public class IvrController {
    private final IvrService service;
    private final IvrAudioService audioService;
    @io.swagger.v3.oas.annotations.Parameter(name = "sort", description = "İsteğe bağlı. Örnek: id,desc. JSON köşeli parantezleri ve tırnak kullanmayın; sıralama istemiyorsanız boş bırakın.")
    @GetMapping public Page<IvrResponse> list(@RequestParam(required = false) Long tenantId, @ParameterObject Pageable page) { return service.list(tenantId, page); }
    @GetMapping("/{id}") public IvrResponse get(@PathVariable Long id) { return service.get(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public IvrResponse create(@Valid @RequestBody CreateIvrRequest request) { return service.create(request); }
    @PutMapping("/{id}")
    public IvrResponse update(@PathVariable Long id, @Valid @RequestBody UpdateIvrRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
    @PostMapping(value = "/audio", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public com.netgsm.asterisk.dto.response.IvrAudioResponse uploadAudio(
            @RequestParam(required = false) Long tenantId,
            @RequestPart("file") org.springframework.web.multipart.MultipartFile file) {
        return audioService.upload(tenantId, file);
    }
}
