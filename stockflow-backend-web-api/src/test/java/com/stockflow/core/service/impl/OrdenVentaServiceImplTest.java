package com.stockflow.core.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.stockflow.core.dto.DetalleOrdenVentaDto;
import com.stockflow.core.dto.OrdenVentaDto;
import com.stockflow.core.dto.ProductoDto;
import com.stockflow.core.dto.UsuarioDto;
import com.stockflow.core.entity.DetalleOrdenVenta;
import com.stockflow.core.entity.InventarioItem;
import com.stockflow.core.entity.OrdenVenta;
import com.stockflow.core.entity.Producto;
import com.stockflow.core.enums.EnumCodigoEstado;
import com.stockflow.core.exception.ConflictException;
import com.stockflow.core.exception.ValidationException;
import com.stockflow.core.repository.DetalleOrdenVentaRepository;
import com.stockflow.core.repository.InventarioItemRepository;
import com.stockflow.core.repository.OrdenVentaRepository;

@ExtendWith(MockitoExtension.class)
class OrdenVentaServiceImplTest {

    @Mock
    private OrdenVentaRepository ordenVentaRepository;
    @Mock
    private DetalleOrdenVentaRepository detalleOrdenVentaRepository;
    @Mock
    private InventarioItemRepository inventarioItemRepository;

    @InjectMocks
    private OrdenVentaServiceImpl service;

    @Nested
    class GenerateNumeroOrden {
        @Test
        void debeRetornar000001_cuandoNoExisteOrdenPrevio() {
            when(ordenVentaRepository.findTopByOrderByIdDesc()).thenReturn(Optional.empty());

            String numero = service.generateNumeroOrden();

            assertEquals("OV-" + Year.now().getValue() + "-000001", numero);
        }
    }

    @Nested
    class Insert {

        @Test
        void debeReservarStockDistribuyendoEntreItems_cuandoStockEsSuficiente() {
            OrdenVentaDto dto = buildOrdenVentaValidaConUnDetalle(10);

            when(ordenVentaRepository.save(any())).thenAnswer(inv -> {
                OrdenVenta saved = inv.getArgument(0, OrdenVenta.class);
                saved.setId(200);
                return saved;
            });

            InventarioItem item1 = InventarioItem.builder().id(1).cantidad(5).cantidadReservada(0).build();
            InventarioItem item2 = InventarioItem.builder().id(2).cantidad(7).cantidadReservada(1).build();
            when(inventarioItemRepository.findForUpdateByProductoId(eq(3))).thenReturn(List.of(item1, item2));

            OrdenVentaDto result = service.insert(dto);

            assertNotNull(result);
            verify(detalleOrdenVentaRepository).deleteByOrdenVentaId(200);

            ArgumentCaptor<InventarioItem> captor = ArgumentCaptor.forClass(InventarioItem.class);
            verify(inventarioItemRepository, times(2)).save(captor.capture());

            List<InventarioItem> savedItems = captor.getAllValues();
            assertEquals(0, savedItems.get(0).getCantidad());
            assertEquals(5, savedItems.get(0).getCantidadReservada());

            assertEquals(2, savedItems.get(1).getCantidad());
            assertEquals(6, savedItems.get(1).getCantidadReservada());
        }

        @Test
        void debeLanzarExcepcion_cuandoStockNoEsSuficiente() {
            OrdenVentaDto dto = buildOrdenVentaValidaConUnDetalle(5);

            when(ordenVentaRepository.save(any())).thenAnswer(inv -> {
                OrdenVenta saved = inv.getArgument(0, OrdenVenta.class);
                saved.setId(200);
                return saved;
            });

            InventarioItem item1 = InventarioItem.builder().id(1).cantidad(2).cantidadReservada(0).build();
            InventarioItem item2 = InventarioItem.builder().id(2).cantidad(1).cantidadReservada(0).build();
            when(inventarioItemRepository.findForUpdateByProductoId(eq(3))).thenReturn(List.of(item1, item2));

            assertThrows(ValidationException.class, () -> service.insert(dto));
            verify(detalleOrdenVentaRepository, never()).saveAll(any());
        }
    }

    @Nested
    class Update {

        @Test
        void debeLanzarConflictException_cuandoVersionNoCoincide() {
            OrdenVentaDto dto = buildOrdenVentaValidaConUnDetalle(1);
            dto.setId(10);
            dto.setVersion(1);

            OrdenVenta bd = OrdenVenta.builder()
                    .id(10)
                    .version(2)
                    .build();

            when(ordenVentaRepository.findById(10)).thenReturn(Optional.of(bd));

            assertThrows(ConflictException.class, () -> service.update(dto));
            verify(ordenVentaRepository, never()).saveAndFlush(any());
        }
    }

    @Nested
    class Delete {

        @Test
        void debeRevertirStockYAnularOrden_cuandoExiste() {
            Producto producto = Producto.builder().id(3).nombre("Prod").build();
            DetalleOrdenVenta det = DetalleOrdenVenta.builder().id(1).producto(producto).cantidad(4).build();
            when(detalleOrdenVentaRepository.findByOrdenVentaId(5)).thenReturn(List.of(det));

            InventarioItem item = InventarioItem.builder().id(10).cantidad(10).cantidadReservada(3).build();
            when(inventarioItemRepository.findForUpdateByProductoId(3)).thenReturn(List.of(item));

            when(ordenVentaRepository.findById(5)).thenReturn(Optional.of(OrdenVenta.builder().id(5).build()));
            when(ordenVentaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0, OrdenVenta.class));

            service.delete(5);

            verify(inventarioItemRepository).save(item);
            assertEquals(14, item.getCantidad());
            assertEquals(0, item.getCantidadReservada());

            verify(detalleOrdenVentaRepository).deleteByOrdenVentaId(5);
            ArgumentCaptor<OrdenVenta> captor = ArgumentCaptor.forClass(OrdenVenta.class);
            verify(ordenVentaRepository).save(captor.capture());
            assertEquals(EnumCodigoEstado.ANULADA.getCodigo(), captor.getValue().getEstado());
        }
    }

    private static OrdenVentaDto buildOrdenVentaValidaConUnDetalle(int cantidadDetalle) {
        OrdenVentaDto dto = new OrdenVentaDto();
        dto.setNumeroOrden("OV-TEST-000001");
        dto.setClienteNombre("Cliente");
        dto.setClienteTelefono("999");
        dto.setDireccion("Dir");
        dto.setFechaVenta(LocalDateTime.now());
        dto.setEstado(EnumCodigoEstado.PENDIENTE_DESPACHO.getCodigo());
        dto.setTotalVenta(new BigDecimal("0"));

        UsuarioDto usuario = new UsuarioDto();
        usuario.setId(2);
        dto.setUsuario(usuario);

        ProductoDto producto = new ProductoDto();
        producto.setId(3);

        DetalleOrdenVentaDto det = new DetalleOrdenVentaDto();
        det.setProducto(producto);
        det.setCantidad(cantidadDetalle);
        det.setPrecioUnitario(new BigDecimal("10.00"));

        dto.setDetallesOrdenVenta(List.of(det));
        return dto;
    }
}

