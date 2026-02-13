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
import com.stockflow.core.utils.common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Inicio de sesión exitoso", response));
    }

    @GetMapping("/checkStatus")
    public ResponseEntity<ApiResponse> checkStatus(Authentication authentication) {
        AuthResponse response = authService.checkStatus(authentication);
        return ResponseEntity.ok(ApiResponse.ok("Estado de autenticación", response));
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<ApiResponse> refresh(@RequestBody RequestTokenRefresh request) {
        return ResponseEntity.ok(ApiResponse.ok("Token refrescado exitosamente", authService.refreshToken(request)));
    }
}
