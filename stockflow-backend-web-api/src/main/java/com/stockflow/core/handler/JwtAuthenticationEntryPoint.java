package com.stockflow.core.handler;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockflow.core.utils.common.ApiResponse;
import com.stockflow.core.utils.common.TypeException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        ApiResponse apiResponse = ApiResponse.builder()
                .error(true)
                .titulo("No autorizado")
                .mensaje("No tiene credenciales válidas para acceder a este recurso.")
                .codigo("401")
                .type(TypeException.E)
                .build();
        
        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }
}
