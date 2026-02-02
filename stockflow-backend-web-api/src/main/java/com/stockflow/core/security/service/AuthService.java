package com.stockflow.core.security.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.stockflow.core.repository.UsuarioRepository;
import com.stockflow.core.security.dto.LoginRequest;
import com.stockflow.core.security.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
   private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UsuarioRepository userRepository;

    public String login(LoginRequest request) {
        
        // 1. Validar credenciales (Esto dispara el UserDetailsService internamente)
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // 2. Si la línea anterior falla, Spring lanza una excepción 401. 
        // Si continua, es que todo está bien.
        var user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // 3. Generar el token para ese usuario
        return jwtTokenProvider.generateToken(user);
    }
}
