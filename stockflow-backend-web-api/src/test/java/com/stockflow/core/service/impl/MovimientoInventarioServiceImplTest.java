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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.stockflow.core.dto.MovimientoInventarioDto;
import com.stockflow.core.dto.ProductoDto;
import com.stockflow.core.dto.UbicacionDto;
import com.stockflow.core.dto.UsuarioDto;
import com.stockflow.core.entity.InventarioItem;
import com.stockflow.core.entity.DetalleOrdenCompra;
import com.stockflow.core.entity.DetalleOrdenVenta;
import com.stockflow.core.entity.OrdenCompra;
import com.stockflow.core.entity.OrdenVenta;
import com.stockflow.core.entity.MovimientoInventario;
import com.stockflow.core.entity.Producto;
import com.stockflow.core.entity.Ubicacion;
import com.stockflow.core.entity.Usuario;
import com.stockflow.core.enums.EnumCodigoEstado;
import com.stockflow.core.exception.ValidationException;
import com.stockflow.core.repository.DetalleOrdenCompraRepository;
import com.stockflow.core.repository.OrdenCompraRepository;
import com.stockflow.core.repository.DetalleOrdenVentaRepository;
import com.stockflow.core.repository.OrdenVentaRepository;
import com.stockflow.core.repository.InventarioItemRepository;
import com.stockflow.core.repository.MovimientoInventarioRepository;
import com.stockflow.core.repository.ProductoRepository;
import com.stockflow.core.repository.UbicacionRepository;
import com.stockflow.core.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class MovimientoInventarioServiceImplTest {

    @Mock
    private MovimientoInventarioRepository movimientoInventarioRepository;
    @Mock
    private InventarioItemRepository inventarioItemRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private UbicacionRepository ubicacionRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private OrdenCompraRepository ordenCompraRepository;
    @Mock
    private DetalleOrdenCompraRepository detalleOrdenCompraRepository;
    @Mock
    private OrdenVentaRepository ordenVentaRepository;
    @Mock
    private DetalleOrdenVentaRepository detalleOrdenVentaRepository;

    @InjectMocks
    private MovimientoInventarioServiceImpl service;

    @Nested
    class InsertAll {
        @Test
        void debeLanzarExcepcion_cuandoListaEsVacia() {
            assertThrows(ValidationException.class, () -> service.insertAll(Collections.emptyList()));
        }
    }

    @Nested
    class Insert {

        @Test
        void debeLanzarExcepcion_cuandoTipoMovimientoNoEsValido() {
            MovimientoInventarioDto dto = buildMovimientoBase();
            dto.setTipoMovimiento("NO_EXISTE");
            dto.setCantidad(1);

            assertThrows(ValidationException.class, () -> service.insert(dto));
            verify(movimientoInventarioRepository, never()).save(any());
        }

        @Test
        void debeCrearInventario_cuandoEsEntradaYNoExisteItem() {
            MovimientoInventarioDto dto = buildMovimientoBase();
            dto.setTipoMovimiento(EnumCodigoEstado.ENTRADA.getCodigo());
            dto.setCantidad(5);

            when(movimientoInventarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0, MovimientoInventario.class));
            when(inventarioItemRepository.findForUpdateByProductoIdAndUbicacionId(eq(10), eq(20))).thenReturn(List.of());

            MovimientoInventarioDto saved = service.insert(dto);

            assertNotNull(saved);
            ArgumentCaptor<InventarioItem> captor = ArgumentCaptor.forClass(InventarioItem.class);
            verify(inventarioItemRepository).save(captor.capture());
            assertEquals(5, captor.getValue().getCantidad());
        }

        @Test
        void debeActualizarFechaUltimoConteo_cuandoEsAjusteEntradaInventarioMensual() {
            MovimientoInventarioDto dto = buildMovimientoBase();
            dto.setTipoMovimiento(EnumCodigoEstado.AJUSTE_ENTRADA_INVENTARIO.getCodigo());
            dto.setCantidad(5);

            when(movimientoInventarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0, MovimientoInventario.class));
            when(inventarioItemRepository.findForUpdateByProductoIdAndUbicacionId(eq(10), eq(20))).thenReturn(List.of());

            MovimientoInventarioDto saved = service.insert(dto);

            assertNotNull(saved);
            ArgumentCaptor<InventarioItem> captor = ArgumentCaptor.forClass(InventarioItem.class);
            verify(inventarioItemRepository).save(captor.capture());
            assertNotNull(captor.getValue().getFechaUltimoConteo());
        }

        @Test
        void debeLanzarExcepcion_cuandoEsSalidaYNoExisteInventarioEnUbicacion() {
            MovimientoInventarioDto dto = buildMovimientoBase();
            dto.setTipoMovimiento(EnumCodigoEstado.SALIDA.getCodigo());
            dto.setCantidad(2);

            when(movimientoInventarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0, MovimientoInventario.class));
            when(inventarioItemRepository.findForUpdateByProductoIdAndUbicacionId(eq(10), eq(20))).thenReturn(List.of());

            assertThrows(ValidationException.class, () -> service.insert(dto));
            verify(inventarioItemRepository, never()).save(any());
        }

        @Test
        void debeActualizarFechaUltimoConteo_cuandoEsAjusteSalidaInventarioMensual() {
            MovimientoInventarioDto dto = buildMovimientoBase();
            dto.setTipoMovimiento(EnumCodigoEstado.AJUSTE_SALIDA_INVENTARIO.getCodigo());
            dto.setCantidad(1);

            when(movimientoInventarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0, MovimientoInventario.class));

            InventarioItem item = InventarioItem.builder()
                    .id(100)
                    .producto(Producto.builder().id(10).build())
                    .ubicacion(Ubicacion.builder().id(20).build())
                    .cantidad(5)
                    .cantidadReservada(0)
                    .build();
            when(inventarioItemRepository.findForUpdateByProductoIdAndUbicacionId(eq(10), eq(20))).thenReturn(List.of(item));

            MovimientoInventarioDto saved = service.insert(dto);

            assertNotNull(saved);
            ArgumentCaptor<InventarioItem> captor = ArgumentCaptor.forClass(InventarioItem.class);
            verify(inventarioItemRepository).save(captor.capture());
            assertEquals(4, captor.getValue().getCantidad());
            assertNotNull(captor.getValue().getFechaUltimoConteo());
        }

        @Test
        void debeActualizarFechaUltimoConteo_yNoDespacharOrdenVenta_cuandoEsAjusteSalidaAunqueReferenciaSeaOV() {
            MovimientoInventarioDto dto = buildMovimientoBase();
            dto.setReferencia("OV-2026-001");
            dto.setTipoMovimiento(EnumCodigoEstado.AJUSTE_SALIDA_INVENTARIO.getCodigo());
            dto.setCantidad(1);

            when(movimientoInventarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0, MovimientoInventario.class));

            InventarioItem item = InventarioItem.builder()
                    .id(100)
                    .producto(Producto.builder().id(10).build())
                    .ubicacion(Ubicacion.builder().id(20).build())
                    .cantidad(5)
                    .cantidadReservada(0)
                    .build();
            when(inventarioItemRepository.findForUpdateByProductoIdAndUbicacionId(eq(10), eq(20))).thenReturn(List.of(item));

            MovimientoInventarioDto saved = service.insert(dto);

            assertNotNull(saved);
            ArgumentCaptor<InventarioItem> captor = ArgumentCaptor.forClass(InventarioItem.class);
            verify(inventarioItemRepository).save(captor.capture());
            assertEquals(4, captor.getValue().getCantidad());
            assertEquals(0, captor.getValue().getCantidadReservada());
            assertNotNull(captor.getValue().getFechaUltimoConteo());
            verify(ordenVentaRepository, never()).findByNumeroOrden(any());
            verify(detalleOrdenVentaRepository, never()).findByOrdenVentaId(any());
            verify(detalleOrdenVentaRepository, never()).save(any());
            verify(ordenVentaRepository, never()).save(any());
        }

        @Test
        void debeActualizarInventario_cuandoSalidaDejaCantidadYCantidadReservadaEnCero() {
            MovimientoInventarioDto dto = buildMovimientoBase();
            dto.setTipoMovimiento(EnumCodigoEstado.SALIDA.getCodigo());
            dto.setCantidad(1);

            when(movimientoInventarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0, MovimientoInventario.class));

            InventarioItem item = InventarioItem.builder()
                    .id(100)
                    .producto(Producto.builder().id(10).build())
                    .ubicacion(Ubicacion.builder().id(20).build())
                    .cantidad(1)
                    .cantidadReservada(0)
                    .build();
            when(inventarioItemRepository.findForUpdateByProductoIdAndUbicacionId(eq(10), eq(20))).thenReturn(List.of(item));

            MovimientoInventarioDto saved = service.insert(dto);
            assertNotNull(saved);
            verify(inventarioItemRepository, never()).delete(any());
            ArgumentCaptor<InventarioItem> captor = ArgumentCaptor.forClass(InventarioItem.class);
            verify(inventarioItemRepository).save(captor.capture());
            assertEquals(0, captor.getValue().getCantidad());
            assertEquals(0, captor.getValue().getCantidadReservada());
        }


        @Test
        void debeMarcarDetalleYOrdenComoRecibidaCompleta_cuandoCantidadRecibidaIgualSolicitada() {
            MovimientoInventarioDto dto = buildMovimientoBase();
            dto.setReferencia("OC-2026-001");
            dto.setTipoMovimiento(EnumCodigoEstado.ENTRADA.getCodigo());
            dto.setCantidad(10);

            OrdenCompra oc = OrdenCompra.builder()
                    .id(1)
                    .numeroOrden("OC-2026-001")
                    .estado(EnumCodigoEstado.PENDIENTE_RECEPCION.getCodigo())
                    .build();
            when(ordenCompraRepository.findByNumeroOrden("OC-2026-001")).thenReturn(Optional.of(oc));

            DetalleOrdenCompra det = DetalleOrdenCompra.builder()
                    .id(10)
                    .cantidad(10)
                    .cantidadRecibida(0)
                    .estadoDetalle(EnumCodigoEstado.PENDIENTE_RECEPCION.getCodigo())
                    .producto(Producto.builder().id(10).build())
                    .ordenCompra(oc)
                    .build();
            when(detalleOrdenCompraRepository.findByOrdenCompraId(1)).thenReturn(List.of(det));
            when(detalleOrdenCompraRepository.save(any())).thenAnswer(inv -> inv.getArgument(0, DetalleOrdenCompra.class));
            when(ordenCompraRepository.save(any())).thenAnswer(inv -> inv.getArgument(0, OrdenCompra.class));

            when(movimientoInventarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0, MovimientoInventario.class));
            when(inventarioItemRepository.findForUpdateByProductoIdAndUbicacionId(eq(10), eq(20))).thenReturn(List.of());

            MovimientoInventarioDto saved = service.insert(dto);

            assertNotNull(saved);
            assertEquals(10, det.getCantidadRecibida());
            assertEquals(EnumCodigoEstado.RECIBIDA_COMPLETA.getCodigo(), det.getEstadoDetalle());
            assertEquals(EnumCodigoEstado.RECIBIDA_COMPLETA.getCodigo(), oc.getEstado());
            verify(detalleOrdenCompraRepository).save(det);
            verify(ordenCompraRepository).save(oc);
        }

        @Test
        void debeDescontarCantidadYReservada_cuandoSalidaEsDespachoDeOrdenVenta() {
            MovimientoInventarioDto dto = buildMovimientoBase();
            dto.setReferencia("OV-2026-001");
            dto.setTipoMovimiento(EnumCodigoEstado.SALIDA.getCodigo());
            dto.setCantidad(2);

            OrdenVenta ov = OrdenVenta.builder().id(50).numeroOrden("OV-2026-001").build();
            when(ordenVentaRepository.findByNumeroOrden("OV-2026-001")).thenReturn(Optional.of(ov));
            DetalleOrdenVenta det = DetalleOrdenVenta.builder()
                    .id(60)
                    .ordenVenta(ov)
                    .producto(Producto.builder().id(10).build())
                    .cantidad(5)
                    .cantidadDespachada(0)
                    .estadoDetalle(EnumCodigoEstado.PENDIENTE_DESPACHO.getCodigo())
                    .build();
            when(detalleOrdenVentaRepository.findByOrdenVentaId(50)).thenReturn(List.of(det));
            when(detalleOrdenVentaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0, DetalleOrdenVenta.class));
            when(ordenVentaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0, OrdenVenta.class));

            when(movimientoInventarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0, MovimientoInventario.class));

            InventarioItem item = InventarioItem.builder()
                    .id(100)
                    .producto(Producto.builder().id(10).build())
                    .ubicacion(Ubicacion.builder().id(20).build())
                    .cantidad(10)
                    .cantidadReservada(5)
                    .build();
            when(inventarioItemRepository.findForUpdateByProductoIdAndUbicacionId(eq(10), eq(20))).thenReturn(List.of(item));

            MovimientoInventarioDto saved = service.insert(dto);

            assertNotNull(saved);
            ArgumentCaptor<InventarioItem> captor = ArgumentCaptor.forClass(InventarioItem.class);
            verify(inventarioItemRepository).save(captor.capture());
            assertEquals(8, captor.getValue().getCantidad());
            assertEquals(3, captor.getValue().getCantidadReservada());
        }

        @Test
        void debeLanzarExcepcion_cuandoDespachoOrdenVenta_noTieneReservaSuficienteEnUbicacion() {
            MovimientoInventarioDto dto = buildMovimientoBase();
            dto.setReferencia("OV-2026-001");
            dto.setTipoMovimiento(EnumCodigoEstado.SALIDA.getCodigo());
            dto.setCantidad(2);

            OrdenVenta ov = OrdenVenta.builder().id(50).numeroOrden("OV-2026-001").build();
            when(ordenVentaRepository.findByNumeroOrden("OV-2026-001")).thenReturn(Optional.of(ov));
            DetalleOrdenVenta det = DetalleOrdenVenta.builder()
                    .id(60)
                    .ordenVenta(ov)
                    .producto(Producto.builder().id(10).build())
                    .cantidad(5)
                    .cantidadDespachada(0)
                    .estadoDetalle(EnumCodigoEstado.PENDIENTE_DESPACHO.getCodigo())
                    .build();
            when(detalleOrdenVentaRepository.findByOrdenVentaId(50)).thenReturn(List.of(det));

            when(movimientoInventarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0, MovimientoInventario.class));

            InventarioItem item = InventarioItem.builder()
                    .id(100)
                    .producto(Producto.builder().id(10).build())
                    .ubicacion(Ubicacion.builder().id(20).build())
                    .cantidad(10)
                    .cantidadReservada(1)
                    .build();
            when(inventarioItemRepository.findForUpdateByProductoIdAndUbicacionId(eq(10), eq(20))).thenReturn(List.of(item));

            assertThrows(ValidationException.class, () -> service.insert(dto));
            verify(inventarioItemRepository, never()).save(any());
        }
    }

    @Nested
    class Update {

        @Test
        void debeRevertirInventarioYPetardear_cuandoNuevaCantidadEsNoPositiva() {
            MovimientoInventario old = MovimientoInventario.builder()
                    .id(1)
                    .producto(Producto.builder().id(10).build())
                    .ubicacion(Ubicacion.builder().id(20).build())
                    .usuario(Usuario.builder().id(30).build())
                    .tipoMovimiento(EnumCodigoEstado.ENTRADA.getCodigo())
                    .cantidad(5)
                    .build();

            when(movimientoInventarioRepository.findById(1)).thenReturn(Optional.of(old));

            InventarioItem item = InventarioItem.builder().id(100).producto(old.getProducto()).ubicacion(old.getUbicacion()).cantidad(5).cantidadReservada(0).build();
            when(inventarioItemRepository.findForUpdateByProductoIdAndUbicacionId(10, 20)).thenReturn(List.of(item));

            MovimientoInventarioDto dto = new MovimientoInventarioDto();
            dto.setId(1);
            dto.setCantidad(0);

            assertThrows(ValidationException.class, () -> service.update(dto));

            ArgumentCaptor<InventarioItem> captor = ArgumentCaptor.forClass(InventarioItem.class);
            verify(inventarioItemRepository).save(captor.capture());
            assertEquals(0, captor.getValue().getCantidad());

            verify(movimientoInventarioRepository, never()).save(any());
        }

        @Test
        void debeActualizarRelaciones_cuandoSeEnviaProductoUbicacionUsuario() {
            MovimientoInventario old = MovimientoInventario.builder()
                    .id(1)
                    .producto(Producto.builder().id(10).build())
                    .ubicacion(Ubicacion.builder().id(20).build())
                    .usuario(Usuario.builder().id(30).build())
                    .tipoMovimiento(EnumCodigoEstado.ENTRADA.getCodigo())
                    .cantidad(1)
                    .build();

            when(movimientoInventarioRepository.findById(1)).thenReturn(Optional.of(old));

            InventarioItem item = InventarioItem.builder().id(100).producto(old.getProducto()).ubicacion(old.getUbicacion()).cantidad(10).cantidadReservada(0).build();
            when(inventarioItemRepository.findForUpdateByProductoIdAndUbicacionId(10, 20)).thenReturn(List.of(item));
            when(inventarioItemRepository.findForUpdateByProductoIdAndUbicacionId(11, 22)).thenReturn(List.of(item));

            Producto newProducto = Producto.builder().id(11).build();
            Ubicacion newUbicacion = Ubicacion.builder().id(22).build();
            Usuario newUsuario = Usuario.builder().id(33).build();
            when(productoRepository.findById(11)).thenReturn(Optional.of(newProducto));
            when(ubicacionRepository.findById(22)).thenReturn(Optional.of(newUbicacion));
            when(usuarioRepository.findById(33)).thenReturn(Optional.of(newUsuario));

            when(movimientoInventarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0, MovimientoInventario.class));

            MovimientoInventarioDto dto = new MovimientoInventarioDto();
            dto.setId(1);
            dto.setCantidad(2);

            ProductoDto pDto = new ProductoDto();
            pDto.setId(11);
            dto.setProducto(pDto);

            UbicacionDto uDto = new UbicacionDto();
            uDto.setId(22);
            dto.setUbicacion(uDto);

            UsuarioDto usrDto = new UsuarioDto();
            usrDto.setId(33);
            dto.setUsuario(usrDto);

            MovimientoInventarioDto updated = service.update(dto);

            assertNotNull(updated);
            assertEquals(11, old.getProducto().getId());
            assertEquals(22, old.getUbicacion().getId());
            assertEquals(33, old.getUsuario().getId());
            verify(movimientoInventarioRepository).save(old);
            verify(inventarioItemRepository, times(2)).save(any());
        }
    }

    private static MovimientoInventarioDto buildMovimientoBase() {
        MovimientoInventarioDto dto = new MovimientoInventarioDto();

        ProductoDto producto = new ProductoDto();
        producto.setId(10);
        dto.setProducto(producto);

        UbicacionDto ubicacion = new UbicacionDto();
        ubicacion.setId(20);
        dto.setUbicacion(ubicacion);

        UsuarioDto usuario = new UsuarioDto();
        usuario.setId(30);
        dto.setUsuario(usuario);

        dto.setMotivo("motivo");
        dto.setReferencia("ref");
        return dto;
    }
}
