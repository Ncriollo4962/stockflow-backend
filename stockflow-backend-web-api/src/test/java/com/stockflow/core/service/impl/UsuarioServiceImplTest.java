package com.stockflow.core.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.stockflow.core.dto.UsuarioDto;
import com.stockflow.core.entity.Usuario;
import com.stockflow.core.exception.ConflictException;
import com.stockflow.core.exception.ValidationException;
import com.stockflow.core.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl service;

    @Nested
    class Insert {
        @Test
        void debeEncriptarContrasena_yAsignarEstadoTrue_yLimpiarIdVersion() {
            UsuarioDto dto = buildDtoBase();
            dto.setId(99);
            dto.setVersion(9);
            dto.setEstado(null);
            dto.setContrasena("123");

            when(passwordEncoder.encode(eq("123"))).thenReturn("ENC");
            when(userRepository.save(any())).thenAnswer(inv -> {
                Usuario arg = inv.getArgument(0, Usuario.class);
                return Usuario.builder()
                        .id(10)
                        .version(1)
                        .codigo(arg.getCodigo())
                        .nombre(arg.getNombre())
                        .email(arg.getEmail())
                        .contrasena(arg.getContrasena())
                        .rol(arg.getRol())
                        .estado(arg.getEstado())
                        .build();
            });

            UsuarioDto result = service.insert(dto);

            assertNotNull(result);
            assertEquals(10, result.getId());

            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
            verify(userRepository).save(captor.capture());
            Usuario toSave = captor.getValue();

            assertEquals(null, toSave.getId());
            assertEquals(null, toSave.getVersion());
            assertEquals(Boolean.TRUE, toSave.getEstado());
            assertEquals("ENC", toSave.getContrasena());
        }

        @Test
        void debeLanzarExcepcion_cuandoContrasenaEsNula() {
            UsuarioDto dto = buildDtoBase();
            dto.setContrasena(null);

            ValidationException ex = assertThrows(ValidationException.class, () -> service.insert(dto));
            assertEquals("La contraseña es obligatoria para nuevos usuarios.", ex.getMessage());
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    class Update {
        @Test
        void debeLanzarExcepcion_cuandoNoExiste() {
            UsuarioDto dto = buildDtoBase();
            dto.setId(10);
            when(userRepository.findById(eq(10))).thenReturn(Optional.empty());

            ValidationException ex = assertThrows(ValidationException.class, () -> service.update(dto));
            assertEquals("Usuario no encontrado con ID: 10", ex.getMessage());
            verify(userRepository, never()).saveAndFlush(any());
        }

        @Test
        void debeLanzarConflictException_cuandoVersionNoCoincide() {
            UsuarioDto dto = buildDtoBase();
            dto.setId(10);
            dto.setVersion(1);

            Usuario bd = Usuario.builder().id(10).version(2).build();
            when(userRepository.findById(eq(10))).thenReturn(Optional.of(bd));

            assertThrows(ConflictException.class, () -> service.update(dto));
            verify(userRepository, never()).saveAndFlush(any());
        }

        @Test
        void debeActualizarRolEnMayusculas_yNoActualizarPassword_cuandoPasswordEsBlank() {
            UsuarioDto dto = buildDtoBase();
            dto.setId(10);
            dto.setVersion(2);
            dto.setRol("admin");
            dto.setContrasena("   ");

            Usuario bd = Usuario.builder().id(10).version(2).rol("USER").contrasena("OLD").build();
            when(userRepository.findById(eq(10))).thenReturn(Optional.of(bd));
            when(userRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0, Usuario.class));

            UsuarioDto result = service.update(dto);

            assertNotNull(result);
            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
            verify(userRepository).saveAndFlush(captor.capture());
            Usuario saved = captor.getValue();

            assertEquals("ADMIN", saved.getRol());
            assertEquals("OLD", saved.getContrasena());
            verify(passwordEncoder, never()).encode(any());
        }

        @Test
        void debeActualizarPassword_cuandoPasswordNoEsBlank() {
            UsuarioDto dto = buildDtoBase();
            dto.setId(10);
            dto.setVersion(2);
            dto.setContrasena("new");

            Usuario bd = Usuario.builder().id(10).version(2).rol("USER").contrasena("OLD").build();
            when(userRepository.findById(eq(10))).thenReturn(Optional.of(bd));
            when(passwordEncoder.encode(eq("new"))).thenReturn("ENC");
            when(userRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0, Usuario.class));

            UsuarioDto result = service.update(dto);

            assertNotNull(result);
            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
            verify(userRepository).saveAndFlush(captor.capture());
            assertEquals("ENC", captor.getValue().getContrasena());
        }
    }

    @Nested
    class Delete {
        @Test
        void debeEliminar_cuandoExiste() {
            when(userRepository.existsById(eq(10))).thenReturn(true);

            service.delete(10);

            verify(userRepository).deleteById(eq(10));
        }

        @Test
        void debeLanzarExcepcion_cuandoNoExiste() {
            when(userRepository.existsById(eq(10))).thenReturn(false);

            ValidationException ex = assertThrows(ValidationException.class, () -> service.delete(10));
            assertEquals("No se encontró el usuario con ID: 10", ex.getMessage());
            verify(userRepository, never()).deleteById(any());
        }
    }

    @Nested
    class Find {
        @Test
        void findById_debeRetornarDto_cuandoExiste() {
            when(userRepository.findById(eq(10))).thenReturn(Optional.of(Usuario.builder().id(10).codigo("U1").nombre("N").email("a@b.com").rol("USER").estado(true).build()));

            UsuarioDto result = service.findById(10);

            assertNotNull(result);
            assertEquals(10, result.getId());
        }

        @Test
        void findByNameUser_debeRetornarDto_cuandoExiste() {
            when(userRepository.findByEmail(eq("a@b.com"))).thenReturn(Optional.of(Usuario.builder().id(10).email("a@b.com").codigo("U1").nombre("N").rol("USER").estado(true).build()));

            UsuarioDto result = service.findByNameUser("a@b.com");

            assertNotNull(result);
            assertEquals(10, result.getId());
        }

        @Test
        void findByNameUser_debeLanzarExcepcion_cuandoNoExiste() {
            when(userRepository.findByEmail(eq("a@b.com"))).thenReturn(Optional.empty());

            ValidationException ex = assertThrows(ValidationException.class, () -> service.findByNameUser("a@b.com"));
            assertEquals("Usuario no encontrado con EMAIL: a@b.com", ex.getMessage());
        }

        @Test
        void findAll_debeRetornarLista() {
            when(userRepository.findAll()).thenReturn(List.of(
                    Usuario.builder().id(1).codigo("U1").nombre("N1").email("1@a.com").rol("USER").estado(true).build(),
                    Usuario.builder().id(2).codigo("U2").nombre("N2").email("2@a.com").rol("ADMIN").estado(true).build()));

            List<UsuarioDto> result = service.findAll();

            assertEquals(2, result.size());
        }
    }

    private static UsuarioDto buildDtoBase() {
        UsuarioDto dto = new UsuarioDto();
        dto.setCodigo("U01");
        dto.setNombre("Nombre");
        dto.setEmail("a@b.com");
        dto.setRol("USER");
        dto.setEstado(true);
        dto.setContrasena("pwd");
        return dto;
    }
}
