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

import com.stockflow.core.dto.UbicacionDto;
import com.stockflow.core.entity.Ubicacion;
import com.stockflow.core.exception.ConflictException;
import com.stockflow.core.exception.ValidationException;
import com.stockflow.core.repository.UbicacionRepository;

@ExtendWith(MockitoExtension.class)
class UbicacionServiceImplTest {

    @Mock
    private UbicacionRepository ubicacionRepository;

    @InjectMocks
    private UbicacionServiceImpl service;

    @Nested
    class Insert {
        @Test
        void debeLanzarConflictException_cuandoCodigoYaExiste() {
            UbicacionDto dto = buildDtoBase();
            when(ubicacionRepository.findByCodigo(eq("U01"))).thenReturn(Optional.of(Ubicacion.builder().id(1).build()));

            assertThrows(ConflictException.class, () -> service.insert(dto));
            verify(ubicacionRepository, never()).save(any());
        }

        @Test
        void debeAsignarEstadoTrue_cuandoEstadoEsNulo() {
            UbicacionDto dto = buildDtoBase();
            dto.setEstado(null);

            when(ubicacionRepository.findByCodigo(eq("U01"))).thenReturn(Optional.empty());
            when(ubicacionRepository.save(any())).thenAnswer(inv -> {
                Ubicacion saved = inv.getArgument(0, Ubicacion.class);
                saved.setId(10);
                return saved;
            });

            UbicacionDto result = service.insert(dto);

            assertNotNull(result);
            assertEquals(10, result.getId());

            ArgumentCaptor<Ubicacion> captor = ArgumentCaptor.forClass(Ubicacion.class);
            verify(ubicacionRepository).save(captor.capture());
            assertEquals(Boolean.TRUE, captor.getValue().getEstado());
        }
    }

    @Nested
    class Update {
        @Test
        void debeLanzarExcepcion_cuandoNoExiste() {
            UbicacionDto dto = new UbicacionDto();
            dto.setId(10);
            when(ubicacionRepository.findById(eq(10))).thenReturn(Optional.empty());

            assertThrows(ValidationException.class, () -> service.update(dto));
            verify(ubicacionRepository, never()).save(any());
        }

        @Test
        void debeLanzarConflictException_cuandoSeCambiaCodigo_yNuevoCodigoExiste() {
            Ubicacion old = Ubicacion.builder().id(10).codigo("U01").nombre("N").build();
            UbicacionDto dto = new UbicacionDto();
            dto.setId(10);
            dto.setCodigo("U02");

            when(ubicacionRepository.findById(eq(10))).thenReturn(Optional.of(old));
            when(ubicacionRepository.findByCodigo(eq("U02"))).thenReturn(Optional.of(Ubicacion.builder().id(99).build()));

            assertThrows(ConflictException.class, () -> service.update(dto));
            verify(ubicacionRepository, never()).save(any());
        }

        @Test
        void debeActualizarCampos_cuandoValido() {
            Ubicacion old = Ubicacion.builder().id(10).codigo("U01").nombre("N").descripcion("D").estado(true).build();
            UbicacionDto dto = new UbicacionDto();
            dto.setId(10);
            dto.setCodigo("U02");
            dto.setNombre("N2");
            dto.setDescripcion("D2");
            dto.setEstado(false);

            when(ubicacionRepository.findById(eq(10))).thenReturn(Optional.of(old));
            when(ubicacionRepository.findByCodigo(eq("U02"))).thenReturn(Optional.empty());
            when(ubicacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0, Ubicacion.class));

            UbicacionDto result = service.update(dto);

            assertNotNull(result);
            ArgumentCaptor<Ubicacion> captor = ArgumentCaptor.forClass(Ubicacion.class);
            verify(ubicacionRepository).save(captor.capture());
            Ubicacion saved = captor.getValue();

            assertEquals("U02", saved.getCodigo());
            assertEquals("N2", saved.getNombre());
            assertEquals("D2", saved.getDescripcion());
            assertEquals(false, saved.getEstado());
        }
    }

    @Nested
    class Delete {
        @Test
        void debeEliminar_cuandoExiste() {
            Ubicacion entity = Ubicacion.builder().id(10).build();
            when(ubicacionRepository.findById(eq(10))).thenReturn(Optional.of(entity));

            service.delete(10);

            verify(ubicacionRepository).delete(eq(entity));
        }

        @Test
        void debeLanzarExcepcion_cuandoNoExiste() {
            when(ubicacionRepository.findById(eq(10))).thenReturn(Optional.empty());

            assertThrows(ValidationException.class, () -> service.delete(10));
            verify(ubicacionRepository, never()).delete(any());
        }
    }

    @Nested
    class Find {
        @Test
        void findAll_debeRetornarLista() {
            when(ubicacionRepository.findAll()).thenReturn(List.of(
                    Ubicacion.builder().id(1).codigo("U1").nombre("N1").build(),
                    Ubicacion.builder().id(2).codigo("U2").nombre("N2").build()));

            List<UbicacionDto> result = service.findAll();

            assertEquals(2, result.size());
        }

        @Test
        void findById_debeRetornarDto_cuandoExiste() {
            when(ubicacionRepository.findById(eq(1))).thenReturn(Optional.of(Ubicacion.builder().id(1).codigo("U1").nombre("N1").build()));

            UbicacionDto result = service.findById(1);

            assertNotNull(result);
            assertEquals(1, result.getId());
        }

        @Test
        void findById_debeLanzarExcepcion_cuandoNoExiste() {
            when(ubicacionRepository.findById(eq(1))).thenReturn(Optional.empty());

            assertThrows(ValidationException.class, () -> service.findById(1));
        }
    }

    private static UbicacionDto buildDtoBase() {
        UbicacionDto dto = new UbicacionDto();
        dto.setCodigo("U01");
        dto.setNombre("Ubicacion");
        dto.setDescripcion("desc");
        dto.setEstado(true);
        return dto;
    }
}

