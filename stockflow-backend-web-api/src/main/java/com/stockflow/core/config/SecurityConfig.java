package com.stockflow.core.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.stockflow.core.handler.JwtAccessDeniedHandler;
import com.stockflow.core.handler.JwtAuthenticationEntryPoint;
import com.stockflow.core.security.jwt.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Deshabilitar CSRF (Indispensable para Stateless)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint) 
                        .accessDeniedHandler(jwtAccessDeniedHandler)      
                )

                // 2. Configuración de rutas
                .authorizeHttpRequests(auth -> auth
                        // Solo el login y la documentación son públicos
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // 1. Autenticación
                .requestMatchers("/api/auth/**").permitAll()
                
                // 2. Swagger y Documentación (Lista completa para evitar el 403)
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/v3/api-docs.yaml").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/swagger-resources/**").permitAll()
                .requestMatchers("/webjars/**").permitAll()

                // 3. Healthcheck de Railway (Crítico)
                .requestMatchers("/").permitAll()
                .requestMatchers("/actuator/health/**").permitAll()

                        // TODO LO DEMÁS requiere autenticación con token
                        .anyRequest().authenticated())

                // 3. Política de sesión STATELESS (No guardamos cookies)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. Conectar nuestro motor de autenticación
                .authenticationProvider(authenticationProvider)

                // 5. ¡EL FILTRO JWT!: Ponemos nuestro filtro antes del estándar
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
