package com.stockflow.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Deshabilitamos CSRF porque las APIs REST no lo necesitan (facilita pruebas en Postman)
                .csrf(csrf -> csrf.disable())

                // 2. Configuramos los permisos de las rutas
                .authorizeHttpRequests(auth -> auth
                        // Permitimos que todas las rutas de la API sean públicas por ahora
                        .requestMatchers("/api/**").permitAll()
                        // Cualquier otra ruta requerirá autenticación
                        .anyRequest().authenticated()
                )

                // 3. Mantenemos el soporte para Basic Auth por si lo necesitas luego
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

}
