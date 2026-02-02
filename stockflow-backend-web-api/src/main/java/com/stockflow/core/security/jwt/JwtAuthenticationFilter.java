package com.stockflow.core.security.jwt;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Filtro que se ejecuta una vez por cada petición HTTP (OncePerRequestFilter).
 * Su misión es interceptar la petición, extraer el JWT y validar al usuario.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        // 1. Obtener el encabezado 'Authorization' de la petición HTTP
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. Condición de Guardia: Si no hay token o no empieza con 'Bearer ', deja pasar al siguiente filtro.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraer el token (los primeros 7 caracteres son "Bearer ")
        jwt = authHeader.substring(7);
        
        // 4. Usar nuestro Provider para sacar el usuario del token (el claim 'sub')
        userEmail = jwtTokenProvider.extractUsername(jwt);

        // 5. Si el usuario existe y NO está ya autenticado en el sistema
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // Buscamos los detalles del usuario en la base de datos
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 6. Validación técnica: ¿El token es íntegro y no ha expirado?
            if (jwtTokenProvider.isTokenValid(jwt, userDetails)) {
                
                // Creamos el objeto de autenticación que Spring Security entiende
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // No pasamos contraseña, el token ya es la prueba de identidad
                        userDetails.getAuthorities() // Los roles/permisos del usuario
                );

                // Añadimos detalles de la petición (como la IP) al objeto de autenticación
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // 7. LA CLAVE: Guardamos al usuario en el Contexto de Seguridad
                // A partir de aquí, Spring considera que el usuario ESTÁ LOGUEADO.
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        // 8. Pasar la petición al siguiente filtro en la cadena (Chain)
        filterChain.doFilter(request, response);
    }
    
}
