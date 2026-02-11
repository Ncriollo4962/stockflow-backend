package com.stockflow.core.security.dto;

import com.stockflow.core.dto.UsuarioDto;

public record AuthResponse(UsuarioDto user, String accessToken, String refreshToken) {}
