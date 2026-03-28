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

import com.stockflow.core.dto.CategoriaDto;
import com.stockflow.core.entity.Categoria;
import com.stockflow.core.exception.ConflictException;
import com.stockflow.core.exception.ValidationException;
import com.stockflow.core.repository.CategoriaRepository;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceImplTest {

    @Mock
    private CategoriaRepository categoryRepository;

    @InjectMocks
    private CategoriaServiceImpl service;

    @Nested
    class Insert {
        @Test
        void debeAsignarEstadoTrue_yLimpiarIdVersion() {
            CategoriaDto dto = new CategoriaDto();
            dto.setId(99);
            dto.setVersion(9);
            dto.setCodigo("C01");
            dto.setNombre("Cat");
            dto.setEstado(null);

            when(categoryRepository.save(any())).thenAnswer(inv -> {
                Categoria arg = inv.getArgument(0, Categoria.class);
                return Categoria.builder()
                        .id(1)
                        .version(1)
                        .codigo(arg.getCodigo())
                        .nombre(arg.getNombre())
                        .descripcion(arg.getDescripcion())
                        .estado(arg.getEstado())
                        .build();
            });

            CategoriaDto result = service.insert(dto);

            assertNotNull(result);
            assertEquals(1, result.getId());

            ArgumentCaptor<Categoria> captor = ArgumentCaptor.forClass(Categoria.class);
            verify(categoryRepository).save(captor.capture());
            Categoria toSave = captor.getValue();

            assertNull(toSave.getId());
            assertNull(toSave.getVersion());
            assertEquals(Boolean.TRUE, toSave.getEstado());
        }

        @Test
        void debeLanzarExcepcion_cuandoCodigoEsNulo() {
            CategoriaDto dto = new CategoriaDto();
            dto.setNombre("Cat");

            ValidationException ex = assertThrows(ValidationException.class, () -> service.insert(dto));
            assertEquals("El código de la categoría es obligatorio.", ex.getMessage());
            verify(categoryRepository, never()).save(any());
        }
    }

    @Nested
    class Update {
        @Test
        void debeLanzarExcepcion_cuandoNoExiste() {
            CategoriaDto dto = buildDtoBase();
            dto.setId(10);
            when(categoryRepository.findById(eq(10))).thenReturn(Optional.empty());

            ValidationException ex = assertThrows(ValidationException.class, () -> service.update(dto));
            assertEquals("Categoría no encontrada con ID: 10", ex.getMessage());
            verify(categoryRepository, never()).saveAndFlush(any());
        }

        @Test
        void debeLanzarConflictException_cuandoVersionNoCoincide() {
            CategoriaDto dto = buildDtoBase();
            dto.setId(10);
            dto.setVersion(1);

            Categoria bd = Categoria.builder().id(10).version(2).build();
            when(categoryRepository.findById(eq(10))).thenReturn(Optional.of(bd));

            assertThrows(ConflictException.class, () -> service.update(dto));
            verify(categoryRepository, never()).saveAndFlush(any());
        }

        @Test
        void debeActualizarCampos_cuandoValido() {
            CategoriaDto dto = buildDtoBase();
            dto.setId(10);
            dto.setVersion(2);
            dto.setCodigo("C02");
            dto.setNombre("Cat 2");
            dto.setDescripcion("desc");
            dto.setEstado(false);

            Categoria bd = Categoria.builder().id(10).version(2).build();
            when(categoryRepository.findById(eq(10))).thenReturn(Optional.of(bd));
            when(categoryRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0, Categoria.class));

            CategoriaDto result = service.update(dto);

            assertNotNull(result);
            ArgumentCaptor<Categoria> captor = ArgumentCaptor.forClass(Categoria.class);
            verify(categoryRepository).saveAndFlush(captor.capture());
            Categoria saved = captor.getValue();

            assertEquals("C02", saved.getCodigo());
            assertEquals("Cat 2", saved.getNombre());
            assertEquals("desc", saved.getDescripcion());
            assertEquals(false, saved.getEstado());
        }
    }

    @Nested
    class Delete {
        @Test
        void debeDesactivar_cuandoExiste() {
            Categoria bd = Categoria.builder().id(10).estado(true).build();
            when(categoryRepository.findById(eq(10))).thenReturn(Optional.of(bd));
            when(categoryRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0, Categoria.class));

            service.delete(10);

            ArgumentCaptor<Categoria> captor = ArgumentCaptor.forClass(Categoria.class);
            verify(categoryRepository).saveAndFlush(captor.capture());
            assertEquals(Boolean.FALSE, captor.getValue().getEstado());
        }

        @Test
        void debeLanzarExcepcion_cuandoIdEsNulo() {
            ValidationException ex = assertThrows(ValidationException.class, () -> service.delete(null));
            assertEquals("No se encontró la Categoría con ID: null", ex.getMessage());
            verify(categoryRepository, never()).saveAndFlush(any());
        }

        @Test
        void debeLanzarExcepcion_cuandoNoExiste() {
            when(categoryRepository.findById(eq(10))).thenReturn(Optional.empty());

            ValidationException ex = assertThrows(ValidationException.class, () -> service.delete(10));
            assertEquals("No se encontró la Categoría con ID: 10", ex.getMessage());
            verify(categoryRepository, never()).saveAndFlush(any());
        }
    }

    @Nested
    class Find {
        @Test
        void findAll_debeRetornarLista() {
            when(categoryRepository.findAll()).thenReturn(List.of(
                    Categoria.builder().id(1).codigo("C1").nombre("N1").build(),
                    Categoria.builder().id(2).codigo("C2").nombre("N2").build()));

            List<CategoriaDto> result = service.findAll();

            assertEquals(2, result.size());
        }

        @Test
        void findById_debeRetornarDto_cuandoExiste() {
            when(categoryRepository.findById(eq(1))).thenReturn(Optional.of(Categoria.builder().id(1).codigo("C1").nombre("N1").build()));

            CategoriaDto result = service.findById(1);

            assertNotNull(result);
            assertEquals(1, result.getId());
        }

        @Test
        void findById_debeLanzarExcepcion_cuandoNoExiste() {
            when(categoryRepository.findById(eq(1))).thenReturn(Optional.empty());

            assertThrows(ValidationException.class, () -> service.findById(1));
        }
    }

    private static CategoriaDto buildDtoBase() {
        CategoriaDto dto = new CategoriaDto();
        dto.setCodigo("C01");
        dto.setNombre("Cat");
        dto.setEstado(true);
        return dto;
    }
}
