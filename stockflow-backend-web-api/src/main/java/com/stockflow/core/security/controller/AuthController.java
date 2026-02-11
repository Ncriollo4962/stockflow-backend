package com.stockflow.core.security.controller;

import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stockflow.core.security.dto.AuthResponse;
import com.stockflow.core.security.dto.LoginRequest;
import com.stockflow.core.security.dto.RequestTokenRefresh;
import com.stockflow.core.security.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    // El AuthService se encargará de validar y generar el token
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/checkStatus")
    public ResponseEntity<AuthResponse> checkStatus(Authentication authentication) {
        AuthResponse response = authService.checkStatus(authentication);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RequestTokenRefresh request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }
}
