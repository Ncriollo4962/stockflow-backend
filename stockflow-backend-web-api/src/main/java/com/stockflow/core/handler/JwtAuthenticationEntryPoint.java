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
        
        // Si el error ya fue manejado y escrito en la respuesta (por ejemplo, por GlobalExceptionHandler), no hacemos nada.
        if (response.isCommitted()) {
            return;
        }

        // Si hay una excepción original que no es de autenticación (ej. error 500 de base de datos),
        // dejamos que Spring Boot o el GlobalExceptionHandler lo maneje en lugar de forzar un 401.
        if (request.getAttribute("javax.servlet.error.exception") != null) {
            throw new ServletException((Exception) request.getAttribute("javax.servlet.error.exception"));
        }
        
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
