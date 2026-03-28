package com.stockflow.core.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

import com.stockflow.core.dto.ProveedorDto;
import com.stockflow.core.entity.Proveedor;
import com.stockflow.core.exception.ConflictException;
import com.stockflow.core.exception.ValidationException;
import com.stockflow.core.repository.ProveedorRepository;

@ExtendWith(MockitoExtension.class)
class ProveedorServiceImplTest {

    @Mock
    private ProveedorRepository proveedorRepository;

    @InjectMocks
    private ProveedorServiceImpl service;

    @Nested
    class Insert {
        @Test
        void debeAsignarEstadoTrue_yLimpiarIdVersion() {
            ProveedorDto dto = buildDtoBase();
            dto.setId(99);
            dto.setVersion(9);
            dto.setEstado(null);

            when(proveedorRepository.findByCodigo(eq("PR1"))).thenReturn(Optional.empty());
            when(proveedorRepository.saveAndFlush(any())).thenAnswer(inv -> {
                Proveedor arg = inv.getArgument(0, Proveedor.class);
                return Proveedor.builder()
                        .id(10)
                        .version(1)
                        .codigo(arg.getCodigo())
                        .nombre(arg.getNombre())
                        .estado(arg.getEstado())
                        .build();
            });

            ProveedorDto result = service.insert(dto);

            assertNotNull(result);
            assertEquals(10, result.getId());

            ArgumentCaptor<Proveedor> captor = ArgumentCaptor.forClass(Proveedor.class);
            verify(proveedorRepository).saveAndFlush(captor.capture());
            Proveedor toSave = captor.getValue();

            assertNull(toSave.getId());
            assertNull(toSave.getVersion());
            assertEquals(Boolean.TRUE, toSave.getEstado());
        }

        @Test
        void debeLanzarExcepcion_cuandoCodigoEsNulo() {
            ProveedorDto dto = buildDtoBase();
            dto.setCodigo(null);

            ValidationException ex = assertThrows(ValidationException.class, () -> service.insert(dto));
            assertEquals("El código del proveedor es obligatorio.", ex.getMessage());
            verify(proveedorRepository, never()).saveAndFlush(any());
        }

        @Test
        void debeLanzarConflictException_cuandoCodigoExiste() {
            ProveedorDto dto = buildDtoBase();
            when(proveedorRepository.findByCodigo(eq("PR1"))).thenReturn(Optional.of(Proveedor.builder().id(1).build()));

            assertThrows(ConflictException.class, () -> service.insert(dto));
            verify(proveedorRepository, never()).saveAndFlush(any());
        }
    }

    @Nested
    class Update {
        @Test
        void debeLanzarExcepcion_cuandoNoExiste() {
            ProveedorDto dto = buildDtoBase();
            dto.setId(10);
            when(proveedorRepository.findById(eq(10))).thenReturn(Optional.empty());

            ValidationException ex = assertThrows(ValidationException.class, () -> service.update(dto));
            assertEquals("Proveedor no encontrado con ID: 10", ex.getMessage());
            verify(proveedorRepository, never()).saveAndFlush(any());
        }

        @Test
        void debeLanzarConflictException_cuandoVersionNoCoincide() {
            ProveedorDto dto = buildDtoBase();
            dto.setId(10);
            dto.setVersion(1);

            Proveedor bd = Proveedor.builder().id(10).version(2).build();
            when(proveedorRepository.findById(eq(10))).thenReturn(Optional.of(bd));

            assertThrows(ConflictException.class, () -> service.update(dto));
            verify(proveedorRepository, never()).saveAndFlush(any());
        }

        @Test
        void debeActualizarCampos_cuandoValido() {
            ProveedorDto dto = buildDtoBase();
            dto.setId(10);
            dto.setVersion(2);
            dto.setCodigo("PR2");
            dto.setNombre("Prov 2");
            dto.setEstado(false);

            Proveedor bd = Proveedor.builder().id(10).version(2).codigo("PR1").estado(true).build();
            when(proveedorRepository.findById(eq(10))).thenReturn(Optional.of(bd));
            when(proveedorRepository.findByCodigo(eq("PR2"))).thenReturn(Optional.empty());
            when(proveedorRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0, Proveedor.class));

            ProveedorDto result = service.update(dto);

            assertNotNull(result);
            ArgumentCaptor<Proveedor> captor = ArgumentCaptor.forClass(Proveedor.class);
            verify(proveedorRepository).saveAndFlush(captor.capture());
            Proveedor saved = captor.getValue();

            assertEquals("PR2", saved.getCodigo());
            assertEquals("Prov 2", saved.getNombre());
            assertEquals(false, saved.getEstado());
        }
    }

    @Nested
    class Delete {
        @Test
        void debeDesactivar_cuandoExiste() {
            Proveedor bd = Proveedor.builder().id(10).estado(true).build();
            when(proveedorRepository.findById(eq(10))).thenReturn(Optional.of(bd));
            when(proveedorRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0, Proveedor.class));

            service.delete(10);

            ArgumentCaptor<Proveedor> captor = ArgumentCaptor.forClass(Proveedor.class);
            verify(proveedorRepository).saveAndFlush(captor.capture());
            assertEquals(Boolean.FALSE, captor.getValue().getEstado());
        }

        @Test
        void debeLanzarExcepcion_cuandoNoExiste() {
            when(proveedorRepository.findById(eq(10))).thenReturn(Optional.empty());

            ValidationException ex = assertThrows(ValidationException.class, () -> service.delete(10));
            assertEquals("No se encontró el proveedor con ID: 10", ex.getMessage());
            verify(proveedorRepository, never()).saveAndFlush(any());
        }
    }

    @Nested
    class Find {
        @Test
        void findAll_debeRetornarListaMapeada() {
            when(proveedorRepository.findAll()).thenReturn(List.of(
                    Proveedor.builder().id(1).codigo("PR1").nombre("Prov 1").build(),
                    Proveedor.builder().id(2).codigo("PR2").nombre("Prov 2").build()));

            List<ProveedorDto> result = service.findAll();

            assertNotNull(result);
            assertEquals(2, result.size());
        }

        @Test
        void findById_debeRetornarDto_cuandoExiste() {
            when(proveedorRepository.findById(eq(10))).thenReturn(Optional.of(Proveedor.builder().id(10).codigo("PR1").nombre("Prov").estado(true).build()));

            ProveedorDto result = service.findById(10);

            assertNotNull(result);
            assertEquals(10, result.getId());
        }

        @Test
        void findById_debeLanzarExcepcion_cuandoNoExiste() {
            when(proveedorRepository.findById(eq(10))).thenReturn(Optional.empty());

            assertThrows(ValidationException.class, () -> service.findById(10));
        }
    }

    private static ProveedorDto buildDtoBase() {
        ProveedorDto dto = new ProveedorDto();
        dto.setCodigo("PR1");
        dto.setNombre("Proveedor");
        dto.setEstado(true);
        return dto;
    }
}
