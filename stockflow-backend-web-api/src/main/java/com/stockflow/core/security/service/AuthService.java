package com.stockflow.core.security.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stockflow.core.dto.UsuarioDto;
import com.stockflow.core.handler.AuthCustomException;
import com.stockflow.core.handler.BusinessException;
import com.stockflow.core.repository.UsuarioRepository;
import com.stockflow.core.security.dto.AuthResponse;
import com.stockflow.core.security.dto.LoginRequest;
import com.stockflow.core.security.dto.RequestTokenRefresh;
import com.stockflow.core.security.jwt.JwtTokenProvider;
import com.stockflow.core.utils.common.TypeException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UsuarioRepository userRepository;
    private final UserDetailsService userDetailsService;

    public AuthResponse login(LoginRequest request) {

        try {
            // Validar credenciales (Esto dispara el UserDetailsService internamente)
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));

            return processAuthResponse(request.email(), null, false);

        } catch (BadCredentialsException e) {
            throw new AuthCustomException("Contraseña incorrecta");
        } catch (InternalAuthenticationServiceException | UsernameNotFoundException e) {
            throw new AuthCustomException("Usuario no encontrado");
        } catch (DisabledException e) {
            throw new AuthCustomException("Tu cuenta está deshabilitada");
        } catch (LockedException e) {
            throw new AuthCustomException("Tu cuenta está bloqueada");
        } catch (AuthenticationException e) {
            throw new AuthCustomException("Error de autenticación: " + e.getMessage());
        } catch (Exception e) {
            throw new BusinessException("Error interno", "500", "Error inesperado en el servidor: " + e.getMessage(), TypeException.E);
        }
    }

    public AuthResponse checkStatus(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthCustomException("Sesión no válida");
        }
        return this.processAuthResponse(authentication.getName(), null, true);
    }

    private AuthResponse processAuthResponse(String email,String tokenRefreshActual, boolean isRefreshToken) {
       return userRepository.findByEmail(email)
                .map(user -> {
      
                    String accessToken = jwtTokenProvider.generateToken(user);
                    String refreshToken = isRefreshToken ? tokenRefreshActual : jwtTokenProvider.generateRefreshToken(user);
                    
                    UsuarioDto userDto = UsuarioDto.build().fromEntity(user);
                    return new AuthResponse(userDto, accessToken, refreshToken);
                })
                .orElseThrow(() -> new AuthCustomException("Usuario con email " + email + " no encontrado"));
    }

    public AuthResponse refreshToken(RequestTokenRefresh request) {
        String refreshToken = request.refreshToken();
        String username = jwtTokenProvider.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtTokenProvider.isRefreshToken(refreshToken)
                || !jwtTokenProvider.isTokenValid(refreshToken, userDetails)) {
            throw new AuthCustomException("Refresh token no válido o expirado");
        }

         return this.processAuthResponse(username, refreshToken, true);
    }
}
