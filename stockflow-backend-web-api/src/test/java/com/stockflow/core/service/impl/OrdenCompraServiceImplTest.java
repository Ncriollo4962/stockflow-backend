package com.stockflow.core.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

import com.stockflow.core.dto.DetalleOrdenCompraDto;
import com.stockflow.core.dto.OrdenCompraDto;
import com.stockflow.core.dto.ProductoDto;
import com.stockflow.core.dto.ProveedorDto;
import com.stockflow.core.dto.UsuarioDto;
import com.stockflow.core.entity.OrdenCompra;
import com.stockflow.core.entity.Proveedor;
import com.stockflow.core.entity.Usuario;
import com.stockflow.core.enums.EnumCodigoEstado;
import com.stockflow.core.exception.ConflictException;
import com.stockflow.core.exception.ValidationException;
import com.stockflow.core.repository.DetalleOrdenCompraRepository;
import com.stockflow.core.repository.OrdenCompraRepository;
import com.stockflow.core.repository.ProveedorRepository;
import com.stockflow.core.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class OrdenCompraServiceImplTest {

    @Mock
    private OrdenCompraRepository ordenCompraRepository;
    @Mock
    private DetalleOrdenCompraRepository detalleOrdenCompraRepository;
    @Mock
    private ProveedorRepository proveedorRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private OrdenCompraServiceImpl service;

    @Nested
    class GenerateNumeroOrden {

        @Test
        void debeRetornar000001_cuandoNoExisteOrdenPrevio() {
            when(ordenCompraRepository.findTopByOrderByIdDesc()).thenReturn(Optional.empty());

            String numero = service.generateNumeroOrden();

            assertEquals("OC-" + Year.now().getValue() + "-000001", numero);
        }

        @Test
        void debeIncrementarCorrelativo_cuandoUltimoNumeroTieneFormatoValido() {
            String year = String.valueOf(Year.now().getValue());
            OrdenCompra last = OrdenCompra.builder().numeroOrden("OC-" + year + "-000010").build();
            when(ordenCompraRepository.findTopByOrderByIdDesc()).thenReturn(Optional.of(last));

            String numero = service.generateNumeroOrden();

            assertEquals("OC-" + year + "-000011", numero);
        }

        @Test
        void debeReiniciar000001_cuandoUltimoNumeroTieneFormatoInvalido() {
            String year = String.valueOf(Year.now().getValue());
            OrdenCompra last = OrdenCompra.builder().numeroOrden("OC-" + year + "-ABC").build();
            when(ordenCompraRepository.findTopByOrderByIdDesc()).thenReturn(Optional.of(last));

            String numero = service.generateNumeroOrden();

            assertEquals("OC-" + year + "-000001", numero);
        }
    }

    @Nested
    class Insert {

        @Test
        void debeLanzarExcepcion_cuandoProveedorNoExiste() {
            OrdenCompraDto dto = buildOrdenCompraValidaConUnDetalle();
            when(proveedorRepository.findById(dto.getProveedor().getId())).thenReturn(Optional.empty());

            ValidationException ex = assertThrows(ValidationException.class, () -> service.insert(dto));
            assertEquals("Proveedor no encontrado con ID: " + dto.getProveedor().getId(), ex.getMessage());
            verify(ordenCompraRepository, never()).saveAndFlush(any());
        }

        @Test
        void debeGuardarOrdenYDetalles_cuandoDatosSonValidos() {
            OrdenCompraDto dto = buildOrdenCompraValidaConUnDetalle();
            Proveedor proveedor = Proveedor.builder().id(dto.getProveedor().getId()).build();
            Usuario usuario = Usuario.builder().id(dto.getUsuario().getId()).build();

            when(proveedorRepository.findById(dto.getProveedor().getId())).thenReturn(Optional.of(proveedor));
            when(usuarioRepository.findById(dto.getUsuario().getId())).thenReturn(Optional.of(usuario));

            when(ordenCompraRepository.saveAndFlush(any())).thenAnswer(inv -> {
                OrdenCompra saved = inv.getArgument(0, OrdenCompra.class);
                saved.setId(999);
                return saved;
            });

            OrdenCompraDto result = service.insert(dto);

            assertNotNull(result);
            assertEquals(999, result.getId());
            verify(detalleOrdenCompraRepository).deleteByOrdenCompraId(999);
            verify(detalleOrdenCompraRepository).saveAll(any());
        }
    }

    @Nested
    class Update {

        @Test
        void debeLanzarConflictException_cuandoVersionNoCoincide() {
            OrdenCompraDto dto = buildOrdenCompraValidaConUnDetalle();
            dto.setId(10);
            dto.setVersion(1);

            OrdenCompra bd = OrdenCompra.builder()
                    .id(10)
                    .version(2)
                    .build();

            when(ordenCompraRepository.findById(10)).thenReturn(Optional.of(bd));

            assertThrows(ConflictException.class, () -> service.update(dto));
            verify(ordenCompraRepository, never()).saveAndFlush(any());
        }
    }

    @Nested
    class Delete {

        @Test
        void debeAnularOrdenYEliminarDetalles_cuandoExiste() {
            when(ordenCompraRepository.findById(5)).thenReturn(Optional.of(OrdenCompra.builder().id(5).build()));
            when(ordenCompraRepository.save(any())).thenAnswer(inv -> inv.getArgument(0, OrdenCompra.class));

            service.delete(5);

            verify(detalleOrdenCompraRepository).deleteByOrdenCompraId(5);
            ArgumentCaptor<OrdenCompra> captor = ArgumentCaptor.forClass(OrdenCompra.class);
            verify(ordenCompraRepository).save(captor.capture());
            assertEquals(EnumCodigoEstado.ANULADA.getCodigo(), captor.getValue().getEstado());
        }
    }

    private static OrdenCompraDto buildOrdenCompraValidaConUnDetalle() {
        OrdenCompraDto dto = new OrdenCompraDto();
        dto.setNumeroOrden("OC-TEST-000001");
        dto.setFechaOrdenCompra(LocalDateTime.now());
        dto.setFechaEntrega(LocalDateTime.now().plusDays(3));
        dto.setEstado(EnumCodigoEstado.PENDIENTE_RECEPCION.getCodigo());
        dto.setTotalCompra(new BigDecimal("100.00"));
        dto.setNotas("nota");

        ProveedorDto proveedor = new ProveedorDto();
        proveedor.setId(1);
        dto.setProveedor(proveedor);

        UsuarioDto usuario = new UsuarioDto();
        usuario.setId(2);
        dto.setUsuario(usuario);

        ProductoDto producto = new ProductoDto();
        producto.setId(3);

        DetalleOrdenCompraDto det = new DetalleOrdenCompraDto();
        det.setProducto(producto);
        det.setCantidad(1);
        det.setPrecioUnitario(new BigDecimal("100.00"));

        dto.setDetallesOrdenCompra(List.of(det));
        return dto;
    }
}

