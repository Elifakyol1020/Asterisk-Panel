package com.netgsm.asterisk.controller;
import com.netgsm.asterisk.dto.CreateUserRequest;
import com.netgsm.asterisk.dto.UpdateUserRequest;
import com.netgsm.asterisk.dto.UserResponse;
import com.netgsm.asterisk.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/admin") @RequiredArgsConstructor
public class UserController {
    private final UserService service;
    @GetMapping("/tenants/{tenantId}/users") public Page<UserResponse> list(@PathVariable Long tenantId, Pageable page) { return service.list(tenantId, page); }
    @GetMapping("/users/{id}") public UserResponse get(@PathVariable Long id) { return service.get(id); }
    @PostMapping("/tenants/{tenantId}/users") @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@PathVariable Long tenantId, @Valid @RequestBody CreateUserRequest request) { return service.create(tenantId, request); }
    @PutMapping("/users/{id}") public UserResponse update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) { return service.update(id, request); }
    @DeleteMapping("/users/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { service.delete(id); }
}
