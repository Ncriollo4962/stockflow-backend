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

import java.math.BigDecimal;
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
import com.stockflow.core.dto.ProductoDto;
import com.stockflow.core.entity.Categoria;
import com.stockflow.core.entity.Producto;
import com.stockflow.core.exception.ConflictException;
import com.stockflow.core.exception.ValidationException;
import com.stockflow.core.repository.CategoriaRepository;
import com.stockflow.core.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {

    @Mock
    private ProductoRepository productRepository;
    @Mock
    private CategoriaRepository categoryRepository;

    @InjectMocks
    private ProductoServiceImpl service;

    @Nested
    class Insert {
        @Test
        void debeAsignarEstadoTrue_yLimpiarIdVersion_cuandoEstadoEsNulo() {
            ProductoDto dto = buildProductoDtoBase();
            dto.setId(999);
            dto.setVersion(9);
            dto.setEstado(null);

            Categoria categoria = Categoria.builder().id(1).nombre("CAT").build();
            when(categoryRepository.findById(eq(1))).thenReturn(Optional.of(categoria));
            when(productRepository.saveAndFlush(any())).thenAnswer(inv -> {
                Producto arg = inv.getArgument(0, Producto.class);
                return Producto.builder()
                        .id(10)
                        .version(1)
                        .codigo(arg.getCodigo())
                        .nombre(arg.getNombre())
                        .descripcion(arg.getDescripcion())
                        .estado(arg.getEstado())
                        .precioCosto(arg.getPrecioCosto())
                        .precioVenta(arg.getPrecioVenta())
                        .cantidadMinima(arg.getCantidadMinima())
                        .categoria(arg.getCategoria())
                        .build();
            });

            ProductoDto result = service.insert(dto);

            assertNotNull(result);
            assertEquals(10, result.getId());

            ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
            verify(productRepository).saveAndFlush(captor.capture());
            Producto toSave = captor.getValue();

            assertNull(toSave.getId());
            assertNull(toSave.getVersion());
            assertEquals(Boolean.TRUE, toSave.getEstado());
            assertEquals(categoria, toSave.getCategoria());
        }

        @Test
        void debeLanzarExcepcion_cuandoCategoriaNoExiste() {
            ProductoDto dto = buildProductoDtoBase();
            when(categoryRepository.findById(eq(1))).thenReturn(Optional.empty());

            ValidationException ex = assertThrows(ValidationException.class, () -> service.insert(dto));
            assertEquals("Categoría no encontrada con ID: 1", ex.getMessage());
            verify(productRepository, never()).saveAndFlush(any());
        }
    }

    @Nested
    class Update {
        @Test
        void debeLanzarExcepcion_cuandoProductoNoExiste() {
            ProductoDto dto = buildProductoDtoBase();
            dto.setId(10);
            when(productRepository.findById(eq(10))).thenReturn(Optional.empty());

            ValidationException ex = assertThrows(ValidationException.class, () -> service.update(dto));
            assertEquals("Producto no encontrado con ID: 10", ex.getMessage());
            verify(productRepository, never()).saveAndFlush(any());
        }

        @Test
        void debeLanzarConflictException_cuandoVersionNoCoincide() {
            ProductoDto dto = buildProductoDtoBase();
            dto.setId(10);
            dto.setVersion(1);

            Producto productoBD = Producto.builder().id(10).version(2).build();
            when(productRepository.findById(eq(10))).thenReturn(Optional.of(productoBD));

            assertThrows(ConflictException.class, () -> service.update(dto));
            verify(productRepository, never()).saveAndFlush(any());
        }

        @Test
        void debeActualizarCampos_yGuardar_cuandoDatosSonValidos() {
            ProductoDto dto = buildProductoDtoBase();
            dto.setId(10);
            dto.setVersion(2);
            dto.setCodigo("P-NEW");
            dto.setNombre("Nuevo Nombre");
            dto.setDescripcion("desc");
            dto.setPrecioCosto(new BigDecimal("1.50"));
            dto.setPrecioVenta(new BigDecimal("3.50"));
            dto.setCantidadMinima(5);
            dto.setEstado(false);

            Categoria categoria = Categoria.builder().id(1).nombre("CAT").build();
            Producto productoBD = Producto.builder().id(10).version(2).estado(true).categoria(Categoria.builder().id(2).build()).build();

            when(productRepository.findById(eq(10))).thenReturn(Optional.of(productoBD));
            when(categoryRepository.findById(eq(1))).thenReturn(Optional.of(categoria));
            when(productRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0, Producto.class));

            ProductoDto result = service.update(dto);

            assertNotNull(result);
            ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
            verify(productRepository).saveAndFlush(captor.capture());
            Producto saved = captor.getValue();

            assertEquals("P-NEW", saved.getCodigo());
            assertEquals("Nuevo Nombre", saved.getNombre());
            assertEquals("desc", saved.getDescripcion());
            assertEquals(new BigDecimal("1.50"), saved.getPrecioCosto());
            assertEquals(new BigDecimal("3.50"), saved.getPrecioVenta());
            assertEquals(5, saved.getCantidadMinima());
            assertEquals(false, saved.getEstado());
            assertEquals(categoria, saved.getCategoria());
        }
    }

    @Nested
    class Delete {
        @Test
        void debeDesactivar_cuandoExiste() {
            Producto bd = Producto.builder().id(10).estado(true).build();
            when(productRepository.findById(eq(10))).thenReturn(Optional.of(bd));
            when(productRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0, Producto.class));

            service.delete(10);

            ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
            verify(productRepository).saveAndFlush(captor.capture());
            assertEquals(Boolean.FALSE, captor.getValue().getEstado());
        }

        @Test
        void debeLanzarExcepcion_cuandoNoExiste() {
            when(productRepository.findById(eq(10))).thenReturn(Optional.empty());

            ValidationException ex = assertThrows(ValidationException.class, () -> service.delete(10));
            assertEquals("No se encontró el producto con ID: 10", ex.getMessage());
            verify(productRepository, never()).saveAndFlush(any());
        }
    }

    @Nested
    class Find {
        @Test
        void findById_debeRetornarDto_cuandoExiste() {
            when(productRepository.findById(eq(10))).thenReturn(Optional.of(Producto.builder().id(10).codigo("P1").nombre("N").build()));

            ProductoDto result = service.findById(10);

            assertNotNull(result);
            assertEquals(10, result.getId());
        }

        @Test
        void findById_debeLanzarExcepcion_cuandoNoExiste() {
            when(productRepository.findById(eq(10))).thenReturn(Optional.empty());

            assertThrows(ValidationException.class, () -> service.findById(10));
        }

        @Test
        void findAll_debeMapearResultados() {
            when(productRepository.findAll()).thenReturn(List.of(
                    Producto.builder().id(1).codigo("P1").nombre("N1").build(),
                    Producto.builder().id(2).codigo("P2").nombre("N2").build()));

            List<ProductoDto> result = service.findAll();

            assertEquals(2, result.size());
        }

        @Test
        void findByCodigo_debeRetornarNull_cuandoNoExiste() {
            when(productRepository.findByCodigo(eq("X"))).thenReturn(Optional.empty());

            ProductoDto result = service.findByCodigo("X");

            assertNull(result);
        }

        @Test
        void findByCodigo_debeRetornarDto_cuandoExiste() {
            when(productRepository.findByCodigo(eq("P1"))).thenReturn(Optional.of(Producto.builder().id(1).codigo("P1").nombre("N1").build()));

            ProductoDto result = service.findByCodigo("P1");

            assertNotNull(result);
            assertEquals(1, result.getId());
        }
    }

    private static ProductoDto buildProductoDtoBase() {
        ProductoDto dto = new ProductoDto();
        dto.setCodigo("P-001");
        dto.setNombre("Producto");
        dto.setPrecioCosto(new BigDecimal("10.00"));
        dto.setPrecioVenta(new BigDecimal("15.00"));
        dto.setCantidadMinima(1);
        dto.setEstado(true);

        CategoriaDto cat = new CategoriaDto();
        cat.setId(1);
        dto.setCategoria(cat);
        return dto;
    }
}
