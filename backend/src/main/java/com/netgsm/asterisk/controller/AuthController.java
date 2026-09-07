package com.netgsm.asterisk.controller;
import com.netgsm.asterisk.dto.request.LoginRequest;
import com.netgsm.asterisk.dto.response.LoginResponse;
import com.netgsm.asterisk.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor
public class AuthController {
    private final AuthService service;
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) { return service.login(request); }
}
