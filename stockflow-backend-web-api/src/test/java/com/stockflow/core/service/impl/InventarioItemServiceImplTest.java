package com.stockflow.core.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.stockflow.core.dto.InventarioItemDto;
import com.stockflow.core.dto.ProductoDto;
import com.stockflow.core.dto.UbicacionDto;
import com.stockflow.core.entity.InventarioItem;
import com.stockflow.core.entity.Producto;
import com.stockflow.core.entity.Ubicacion;
import com.stockflow.core.exception.ConflictException;
import com.stockflow.core.exception.ValidationException;
import com.stockflow.core.repository.InventarioItemRepository;
import com.stockflow.core.repository.ProductoRepository;
import com.stockflow.core.repository.UbicacionRepository;

@ExtendWith(MockitoExtension.class)
class InventarioItemServiceImplTest {

    @Mock
    private InventarioItemRepository inventarioItemRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private UbicacionRepository ubicacionRepository;

    @InjectMocks
    private InventarioItemServiceImpl service;

    @Nested
    class Insert {

        @Test
        void debeLanzarExcepcion_cuandoProductoEsNulo() {
            InventarioItemDto dto = buildDtoBase();
            dto.setProducto(null);

            ValidationException ex = assertThrows(ValidationException.class, () -> service.insert(dto));
            assertEquals("El producto es requerido.", ex.getMessage());
        }

        @Test
        void debeLanzarExcepcion_cuandoCantidadEsNegativa() {
            InventarioItemDto dto = buildDtoBase();
            dto.setCantidad(-1);

            ValidationException ex = assertThrows(ValidationException.class, () -> service.insert(dto));
            assertEquals("La cantidad no puede ser negativa.", ex.getMessage());
        }

        @Test
        void debeLanzarExcepcion_cuandoProductoNoExiste() {
            InventarioItemDto dto = buildDtoBase();
            when(productoRepository.findById(eq(10))).thenReturn(Optional.empty());

            ValidationException ex = assertThrows(ValidationException.class, () -> service.insert(dto));
            assertEquals("Producto no encontrado", ex.getMessage());
            verify(ubicacionRepository, never()).findById(any());
            verify(inventarioItemRepository, never()).save(any());
        }

        @Test
        void debeLanzarExcepcion_cuandoUbicacionNoExiste() {
            InventarioItemDto dto = buildDtoBase();
            when(productoRepository.findById(eq(10))).thenReturn(Optional.of(Producto.builder().id(10).build()));
            when(ubicacionRepository.findById(eq(20))).thenReturn(Optional.empty());

            ValidationException ex = assertThrows(ValidationException.class, () -> service.insert(dto));
            assertEquals("Ubicación no encontrada", ex.getMessage());
            verify(inventarioItemRepository, never()).save(any());
        }

        @Test
        void debeLanzarConflictException_cuandoExisteDuplicadoConLote() {
            InventarioItemDto dto = buildDtoBase();
            dto.setLote("L-01");

            when(productoRepository.findById(eq(10))).thenReturn(Optional.of(Producto.builder().id(10).build()));
            when(ubicacionRepository.findById(eq(20))).thenReturn(Optional.of(Ubicacion.builder().id(20).build()));
            when(inventarioItemRepository.findByProductoIdAndUbicacionIdAndLote(eq(10), eq(20), eq("L-01")))
                    .thenReturn(List.of(InventarioItem.builder().id(1).build()));

            assertThrows(ConflictException.class, () -> service.insert(dto));
            verify(inventarioItemRepository, never()).save(any());
        }

        @Test
        void debeLanzarConflictException_cuandoLoteEsNulo_yExisteDuplicadoSinLote() {
            InventarioItemDto dto = buildDtoBase();
            dto.setLote(null);

            when(productoRepository.findById(eq(10))).thenReturn(Optional.of(Producto.builder().id(10).build()));
            when(ubicacionRepository.findById(eq(20))).thenReturn(Optional.of(Ubicacion.builder().id(20).build()));
            when(inventarioItemRepository.findByProductoIdAndUbicacionIdAndLote(eq(10), eq(20), eq(null)))
                    .thenReturn(List.of());

            when(inventarioItemRepository.findByProductoIdAndUbicacionId(eq(10), eq(20)))
                    .thenReturn(List.of(InventarioItem.builder().id(1).lote(null).build()));

            ConflictException ex = assertThrows(ConflictException.class, () -> service.insert(dto));
            assertEquals("Ya existe un registro de inventario para este producto en esta ubicación sin lote.",
                    ex.getMessage());
            verify(inventarioItemRepository, never()).save(any());
        }

        @Test
        void debeGuardarAsignandoRelaciones_yFechaUltimoConteoPorDefecto() {
            InventarioItemDto dto = buildDtoBase();
            dto.setFechaUltimoConteo(null);
            dto.setId(999);

            Producto producto = Producto.builder().id(10).build();
            Ubicacion ubicacion = Ubicacion.builder().id(20).build();

            when(productoRepository.findById(eq(10))).thenReturn(Optional.of(producto));
            when(ubicacionRepository.findById(eq(20))).thenReturn(Optional.of(ubicacion));
            when(inventarioItemRepository.findByProductoIdAndUbicacionIdAndLote(eq(10), eq(20), eq(null)))
                    .thenReturn(List.of());
            when(inventarioItemRepository.findByProductoIdAndUbicacionId(eq(10), eq(20)))
                    .thenReturn(List.of());

            when(inventarioItemRepository.save(any())).thenAnswer(inv -> {
                InventarioItem arg = inv.getArgument(0, InventarioItem.class);
                return InventarioItem.builder()
                        .id(50)
                        .producto(arg.getProducto())
                        .ubicacion(arg.getUbicacion())
                        .cantidad(arg.getCantidad())
                        .cantidadReservada(arg.getCantidadReservada())
                        .lote(arg.getLote())
                        .fechaUltimoConteo(arg.getFechaUltimoConteo())
                        .fechaActualizacion(arg.getFechaActualizacion())
                        .build();
            });

            InventarioItemDto result = service.insert(dto);

            assertNotNull(result);
            assertEquals(50, result.getId());

            ArgumentCaptor<InventarioItem> captor = ArgumentCaptor.forClass(InventarioItem.class);
            verify(inventarioItemRepository).save(captor.capture());

            InventarioItem toSave = captor.getValue();
            assertEquals(null, toSave.getId());
            assertEquals(producto, toSave.getProducto());
            assertEquals(ubicacion, toSave.getUbicacion());
            assertNotNull(toSave.getFechaUltimoConteo());
        }
    }

    @Nested
    class Update {

        @Test
        void debeLanzarExcepcion_cuandoNoExiste() {
            InventarioItemDto dto = new InventarioItemDto();
            dto.setId(1);
            when(inventarioItemRepository.findById(eq(1))).thenReturn(Optional.empty());

            assertThrows(ValidationException.class, () -> service.update(dto));
            verify(inventarioItemRepository, never()).save(any());
        }

        @Test
        void debeLanzarConflictException_cuandoNuevaCombinacionChocaConOtroRegistro() {
            InventarioItem old = InventarioItem.builder()
                    .id(1)
                    .producto(Producto.builder().id(10).build())
                    .ubicacion(Ubicacion.builder().id(20).build())
                    .lote("L1")
                    .cantidad(1)
                    .cantidadReservada(0)
                    .build();

            InventarioItemDto dto = new InventarioItemDto();
            dto.setId(1);
            ProductoDto newProducto = new ProductoDto();
            newProducto.setId(11);
            dto.setProducto(newProducto);
            dto.setLote("L2");

            when(inventarioItemRepository.findById(eq(1))).thenReturn(Optional.of(old));
            when(inventarioItemRepository.findByProductoIdAndUbicacionIdAndLote(eq(11), eq(20), eq("L2")))
                    .thenReturn(List.of(InventarioItem.builder().id(99).build()));

            assertThrows(ConflictException.class, () -> service.update(dto));
            verify(inventarioItemRepository, never()).save(any());
        }

        @Test
        void debeLanzarConflictException_cuandoLoteEsNulo_yCambioProductoColisionaConOtroSinLote() {
            InventarioItem old = InventarioItem.builder()
                    .id(1)
                    .producto(Producto.builder().id(10).build())
                    .ubicacion(Ubicacion.builder().id(20).build())
                    .lote(null)
                    .cantidad(1)
                    .cantidadReservada(0)
                    .build();

            InventarioItemDto dto = new InventarioItemDto();
            dto.setId(1);
            ProductoDto nuevoProducto = new ProductoDto();
            nuevoProducto.setId(11);
            dto.setProducto(nuevoProducto);

            when(inventarioItemRepository.findById(eq(1))).thenReturn(Optional.of(old));
            when(inventarioItemRepository.findByProductoIdAndUbicacionIdAndLote(eq(11), eq(20), eq(null)))
                    .thenReturn(List.of());
            when(inventarioItemRepository.findByProductoIdAndUbicacionId(eq(11), eq(20)))
                    .thenReturn(List.of(InventarioItem.builder().id(2).lote(null).build()));

            assertThrows(ConflictException.class, () -> service.update(dto));
            verify(inventarioItemRepository, never()).save(any());
        }

        @Test
        void debeLanzarExcepcion_cuandoCantidadEsNegativa() {
            InventarioItem old = InventarioItem.builder()
                    .id(1)
                    .producto(Producto.builder().id(10).build())
                    .ubicacion(Ubicacion.builder().id(20).build())
                    .cantidad(1)
                    .cantidadReservada(0)
                    .build();

            InventarioItemDto dto = new InventarioItemDto();
            dto.setId(1);
            dto.setCantidad(-1);

            when(inventarioItemRepository.findById(eq(1))).thenReturn(Optional.of(old));

            ValidationException ex = assertThrows(ValidationException.class, () -> service.update(dto));
            assertEquals("La cantidad no puede ser negativa.", ex.getMessage());
            verify(inventarioItemRepository, never()).save(any());
        }

        @Test
        void debeLanzarExcepcion_cuandoCantidadReservadaEsNegativa() {
            InventarioItem old = InventarioItem.builder()
                    .id(1)
                    .producto(Producto.builder().id(10).build())
                    .ubicacion(Ubicacion.builder().id(20).build())
                    .cantidad(1)
                    .cantidadReservada(0)
                    .build();

            InventarioItemDto dto = new InventarioItemDto();
            dto.setId(1);
            dto.setCantidadReservada(-1);

            when(inventarioItemRepository.findById(eq(1))).thenReturn(Optional.of(old));

            ValidationException ex = assertThrows(ValidationException.class, () -> service.update(dto));
            assertEquals("La cantidad reservada no puede ser negativa.", ex.getMessage());
            verify(inventarioItemRepository, never()).save(any());
        }

        @Test
        void debeActualizarCamposSimples_cuandoSonValidos() {
            InventarioItem old = InventarioItem.builder()
                    .id(1)
                    .producto(Producto.builder().id(10).build())
                    .ubicacion(Ubicacion.builder().id(20).build())
                    .lote("L1")
                    .cantidad(1)
                    .cantidadReservada(0)
                    .build();

            InventarioItemDto dto = new InventarioItemDto();
            dto.setId(1);
            dto.setCantidad(5);
            dto.setCantidadReservada(2);
            LocalDateTime fechaConteo = LocalDateTime.now().minusDays(1);
            dto.setFechaUltimoConteo(fechaConteo);

            when(inventarioItemRepository.findById(eq(1))).thenReturn(Optional.of(old));
            when(inventarioItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0, InventarioItem.class));

            InventarioItemDto result = service.update(dto);

            assertNotNull(result);
            ArgumentCaptor<InventarioItem> captor = ArgumentCaptor.forClass(InventarioItem.class);
            verify(inventarioItemRepository).save(captor.capture());
            InventarioItem saved = captor.getValue();

            assertEquals(5, saved.getCantidad());
            assertEquals(2, saved.getCantidadReservada());
            assertEquals(fechaConteo, saved.getFechaUltimoConteo());
        }

        @Test
        void debeActualizarProductoYUbicacion_cuandoSeEnvianIds() {
            InventarioItem old = InventarioItem.builder()
                    .id(1)
                    .producto(Producto.builder().id(10).build())
                    .ubicacion(Ubicacion.builder().id(20).build())
                    .lote("L1")
                    .cantidad(1)
                    .cantidadReservada(0)
                    .build();

            InventarioItemDto dto = new InventarioItemDto();
            dto.setId(1);
            ProductoDto nuevoProducto = new ProductoDto();
            nuevoProducto.setId(11);
            dto.setProducto(nuevoProducto);
            UbicacionDto nuevaUbicacion = new UbicacionDto();
            nuevaUbicacion.setId(21);
            dto.setUbicacion(nuevaUbicacion);

            Producto prod = Producto.builder().id(11).build();
            Ubicacion ubi = Ubicacion.builder().id(21).build();

            when(inventarioItemRepository.findById(eq(1))).thenReturn(Optional.of(old));
            when(inventarioItemRepository.findByProductoIdAndUbicacionIdAndLote(eq(11), eq(21), eq("L1")))
                    .thenReturn(List.of());
            when(productoRepository.findById(eq(11))).thenReturn(Optional.of(prod));
            when(ubicacionRepository.findById(eq(21))).thenReturn(Optional.of(ubi));
            when(inventarioItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0, InventarioItem.class));

            InventarioItemDto result = service.update(dto);

            assertNotNull(result);
            ArgumentCaptor<InventarioItem> captor = ArgumentCaptor.forClass(InventarioItem.class);
            verify(inventarioItemRepository).save(captor.capture());
            assertEquals(prod, captor.getValue().getProducto());
            assertEquals(ubi, captor.getValue().getUbicacion());
        }
    }

    @Nested
    class Delete {
        @Test
        void debeEliminar_cuandoExiste() {
            InventarioItem entity = InventarioItem.builder().id(1).cantidad(10).build();
            when(inventarioItemRepository.findById(eq(1))).thenReturn(Optional.of(entity));

            service.delete(1);

            verify(inventarioItemRepository).delete(eq(entity));
        }

        @Test
        void debeLanzarExcepcion_cuandoNoExiste() {
            when(inventarioItemRepository.findById(eq(1))).thenReturn(Optional.empty());

            assertThrows(ValidationException.class, () -> service.delete(1));
            verify(inventarioItemRepository, never()).delete(any());
        }
    }

    @Nested
    class Find {
        @Test
        void findAll_debeMapearLista() {
            when(inventarioItemRepository.findAll()).thenReturn(List.of(
                    InventarioItem.builder().id(1).cantidad(1).build(),
                    InventarioItem.builder().id(2).cantidad(2).build()));

            List<InventarioItemDto> result = service.findAll();

            assertEquals(2, result.size());
        }

        @Test
        void findById_debeRetornarDto_cuandoExiste() {
            when(inventarioItemRepository.findById(eq(1))).thenReturn(Optional.of(InventarioItem.builder().id(1).cantidad(1).build()));

            InventarioItemDto result = service.findById(1);

            assertNotNull(result);
            assertEquals(1, result.getId());
        }

        @Test
        void findById_debeLanzarExcepcion_cuandoNoExiste() {
            when(inventarioItemRepository.findById(eq(1))).thenReturn(Optional.empty());

            assertThrows(ValidationException.class, () -> service.findById(1));
        }
    }

    private static InventarioItemDto buildDtoBase() {
        InventarioItemDto dto = new InventarioItemDto();
        ProductoDto p = new ProductoDto();
        p.setId(10);
        UbicacionDto u = new UbicacionDto();
        u.setId(20);
        dto.setProducto(p);
        dto.setUbicacion(u);
        dto.setCantidad(1);
        dto.setCantidadReservada(0);
        dto.setLote(null);
        return dto;
    }
}
