package com.netgsm.asterisk.controller;
import com.netgsm.asterisk.dto.CreateQueueRequest;
import com.netgsm.asterisk.dto.QueueMemberRequest;
import com.netgsm.asterisk.dto.QueueMemberResponse;
import com.netgsm.asterisk.dto.QueueResponse;
import com.netgsm.asterisk.dto.UpdateQueueRequest;
import com.netgsm.asterisk.service.QueueMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/queues/{queueId}/members") @RequiredArgsConstructor
public class QueueMemberController {
    private final QueueMemberService service;
    @GetMapping public Page<QueueMemberResponse> list(@PathVariable Long queueId, Pageable page) { return service.list(queueId, page); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public QueueMemberResponse create(@PathVariable Long queueId, @Valid @RequestBody QueueMemberRequest request) { return service.create(queueId, request); }
    @DeleteMapping("/{memberId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long queueId, @PathVariable Long memberId) { service.delete(queueId, memberId); }
}
