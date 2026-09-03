package com.netgsm.asterisk.controller;
import com.netgsm.asterisk.dto.CreateQueueRequest;
import com.netgsm.asterisk.dto.QueueMemberRequest;
import com.netgsm.asterisk.dto.QueueMemberResponse;
import com.netgsm.asterisk.dto.QueueResponse;
import com.netgsm.asterisk.dto.UpdateQueueRequest;
import com.netgsm.asterisk.service.QueueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/queues") @RequiredArgsConstructor
public class QueueController {
    private final QueueService service;
    @io.swagger.v3.oas.annotations.Parameter(name = "sort", description = "İsteğe bağlı. Örnek: id,desc. JSON köşeli parantezleri ve tırnak kullanmayın; sıralama istemiyorsanız boş bırakın.")
    @GetMapping public Page<QueueResponse> list(@RequestParam(required = false) Long tenantId, @ParameterObject Pageable page) { return service.list(tenantId, page); }
    @GetMapping("/{id}") public QueueResponse get(@PathVariable Long id) { return service.get(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public QueueResponse create(@Valid @RequestBody CreateQueueRequest request) { return service.create(request); }
    @PutMapping("/{id}")
    public QueueResponse update(@PathVariable Long id, @Valid @RequestBody UpdateQueueRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
