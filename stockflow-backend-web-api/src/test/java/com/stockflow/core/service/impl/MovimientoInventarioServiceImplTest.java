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
import com.stockflow.core.entity.MovimientoInventario;
import com.stockflow.core.entity.Producto;
import com.stockflow.core.entity.Ubicacion;
import com.stockflow.core.entity.Usuario;
import com.stockflow.core.enums.EnumCodigoEstado;
import com.stockflow.core.exception.ValidationException;
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
        void debeLanzarExcepcion_cuandoEsSalidaYNoExisteInventarioEnUbicacion() {
            MovimientoInventarioDto dto = buildMovimientoBase();
            dto.setTipoMovimiento(EnumCodigoEstado.SALIDA.getCodigo());
            dto.setCantidad(2);

            when(movimientoInventarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0, MovimientoInventario.class));
            when(inventarioItemRepository.findForUpdateByProductoIdAndUbicacionId(eq(10), eq(20))).thenReturn(List.of());

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

            verify(inventarioItemRepository).save(item);
            assertEquals(0, item.getCantidad());

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

