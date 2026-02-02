package com.stockflow.core.security.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stockflow.core.security.dto.AuthResponse;
import com.stockflow.core.security.dto.LoginRequest;
import com.stockflow.core.security.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // El AuthService se encargará de validar y generar el token
        String token = authService.login(request);
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
