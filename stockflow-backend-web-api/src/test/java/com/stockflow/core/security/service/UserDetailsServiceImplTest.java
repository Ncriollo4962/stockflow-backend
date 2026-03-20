package com.stockflow.core.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.stockflow.core.entity.Usuario;
import com.stockflow.core.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UsuarioRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl service;

    @Nested
    class LoadUserByUsername {
        @Test
        void debeRetornarUserDetails_cuandoExiste() {
            Usuario usuario = Usuario.builder()
                    .id(1)
                    .codigo("U1")
                    .nombre("Nombre")
                    .email("a@b.com")
                    .contrasena("ENC")
                    .rol("USER")
                    .estado(true)
                    .build();

            when(userRepository.findByEmail(eq("a@b.com"))).thenReturn(Optional.of(usuario));

            UserDetails result = service.loadUserByUsername("a@b.com");

            assertNotNull(result);
            assertEquals("a@b.com", result.getUsername());
        }

        @Test
        void debeLanzarUsernameNotFoundException_cuandoNoExiste() {
            when(userRepository.findByEmail(eq("a@b.com"))).thenReturn(Optional.empty());

            UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                    () -> service.loadUserByUsername("a@b.com"));
            assertEquals("Usuario no encontrado con email: a@b.com", ex.getMessage());
        }
    }
}

