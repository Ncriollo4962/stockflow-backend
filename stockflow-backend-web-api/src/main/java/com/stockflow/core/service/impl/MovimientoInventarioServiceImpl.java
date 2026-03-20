package com.stockflow.core.service.impl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stockflow.core.dto.MovimientoInventarioDto;
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
import com.stockflow.core.service.MovimientoInventarioService;
import com.stockflow.core.utils.ValidationUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MovimientoInventarioServiceImpl implements MovimientoInventarioService {

    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final InventarioItemRepository inventarioItemRepository;
    private final ProductoRepository productoRepository;
    private final UbicacionRepository ubicacionRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public List<MovimientoInventarioDto> insertAll(List<MovimientoInventarioDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            throw new ValidationException("La lista de movimientos no puede estar vacía.");
        }
        return dtos.stream().map(this::insert).toList();
    }

    @Override
    @Transactional
    public MovimientoInventarioDto insert(MovimientoInventarioDto dto) {
        validarCamposBaseMovimientoInventario(dto);

        if (dto.getCantidad() <= 0) {
            throw new ValidationException("La cantidad debe ser mayor a cero.");
        }

        MovimientoInventario entity = dto.toEntity();

        entity.setId(null);
        entity.setVersion(null);

        if (entity.getFechaMovimiento() == null) {
            entity.setFechaMovimiento(LocalDateTime.now());
        }

        MovimientoInventario saved = movimientoInventarioRepository.save(entity);

        updateInventario(saved, false);

        return MovimientoInventarioDto.build().fromEntity(saved);
    }

    @Override
    @Transactional
    public MovimientoInventarioDto update(MovimientoInventarioDto dto) {
        MovimientoInventario oldEntity = movimientoInventarioRepository.findById(dto.getId())
                .orElseThrow(() -> new ValidationException("Movimiento no encontrado"));

        // Revertir efecto anterior
        updateInventario(oldEntity, true);

        // Actualizar campos
        if (dto.getTipoMovimiento() != null)
            oldEntity.setTipoMovimiento(dto.getTipoMovimiento());
        if (dto.getCantidad() != null) {
            if (dto.getCantidad() <= 0)
                throw new ValidationException("La cantidad debe ser mayor a cero.");
            oldEntity.setCantidad(dto.getCantidad());
        }
        if (dto.getMotivo() != null)
            oldEntity.setMotivo(dto.getMotivo());
        if (dto.getReferencia() != null)
            oldEntity.setReferencia(dto.getReferencia());
        if (dto.getNotas() != null)
            oldEntity.setNotas(dto.getNotas());
        if (dto.getFechaMovimiento() != null)
            oldEntity.setFechaMovimiento(dto.getFechaMovimiento());

        if (dto.getProducto() != null && dto.getProducto().getId() != null) {
            Producto producto = productoRepository.findById(dto.getProducto().getId())
                    .orElseThrow(() -> new ValidationException("Producto no encontrado"));
            oldEntity.setProducto(producto);
        }

        if (dto.getUbicacion() != null && dto.getUbicacion().getId() != null) {
            Ubicacion ubicacion = ubicacionRepository.findById(dto.getUbicacion().getId())
                    .orElseThrow(() -> new ValidationException("Ubicación no encontrada"));
            oldEntity.setUbicacion(ubicacion);
        }

        if (dto.getUsuario() != null && dto.getUsuario().getId() != null) {
            Usuario usuario = usuarioRepository.findById(dto.getUsuario().getId())
                    .orElseThrow(() -> new ValidationException("Usuario no encontrado"));
            oldEntity.setUsuario(usuario);
        }

        // Aplicar nuevo efecto
        updateInventario(oldEntity, false);

        MovimientoInventario saved = movimientoInventarioRepository.save(oldEntity);
        return MovimientoInventarioDto.build().fromEntity(saved);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        MovimientoInventario entity = movimientoInventarioRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Movimiento no encontrado"));

        updateInventario(entity, true);

        movimientoInventarioRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoInventarioDto> findAll() {
        return movimientoInventarioRepository.findAll().stream()
                .map(e -> MovimientoInventarioDto.build().fromEntity(e))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MovimientoInventarioDto findById(Integer id) {
        return movimientoInventarioRepository.findById(id)
                .map(e -> MovimientoInventarioDto.build().fromEntity(e))
                .orElseThrow(() -> new ValidationException("Movimiento no encontrado"));
    }

    private void updateInventario(MovimientoInventario mov, boolean isRevert) {
        int qty = mov.getCantidad();
        int multiplier = 0;
        String tipo = mov.getTipoMovimiento();

        List<EnumCodigoEstado> salidas = Arrays.asList(
            EnumCodigoEstado.SALIDA, 
            EnumCodigoEstado.TRANSFERENCIA, 
            EnumCodigoEstado.AJUSTE_SALIDA_INVENTARIO
        );

        List<EnumCodigoEstado> entradas = Arrays.asList(
            EnumCodigoEstado.ENTRADA, 
            EnumCodigoEstado.AJUSTE_ENTRADA_INVENTARIO
        );

        if (entradas.stream().anyMatch(e -> e.name().equalsIgnoreCase(tipo) || e.getCodigo().equalsIgnoreCase(tipo))) {
            multiplier = 1;
        } else if (salidas.stream().anyMatch(e -> e.name().equalsIgnoreCase(tipo) || e.getCodigo().equalsIgnoreCase(tipo))) {
            multiplier = -1;
        } else {
            return;
        }

        if (isRevert) {
            multiplier = -multiplier;
        }

        int change = qty * multiplier;
        if (change == 0)
            return;

        // Usar bloqueo pesimista para evitar conflictos de concurrencia al actualizar stock
        List<InventarioItem> items = inventarioItemRepository.findForUpdateByProductoIdAndUbicacionId(
                mov.getProducto().getId(), mov.getUbicacion().getId());

        InventarioItem itemToUpdate = null;

        if (items.isEmpty()) {
            if (change > 0) {
                // Crear nuevo item
                itemToUpdate = InventarioItem.builder()
                        .producto(mov.getProducto())
                        .ubicacion(mov.getUbicacion())
                        .cantidad(0)
                        .cantidadReservada(0)
                        .build();
            } else {
                throw new ValidationException("No existe inventario para descontar stock en esta ubicación.");
            }
        } else {
            // Usar el primer item encontrado (asumiendo gestión simple sin lotes
            // específicos en movimiento)
            itemToUpdate = items.get(0);
        }

        int newQuantity = itemToUpdate.getCantidad() + change;
        if (newQuantity < 0) {
            throw new ValidationException("Stock insuficiente. Stock actual: " + itemToUpdate.getCantidad()
                    + ", cambio solicitado: " + Math.abs(change));
        }

        itemToUpdate.setCantidad(newQuantity);
        inventarioItemRepository.save(itemToUpdate);
    }

    private void validarCamposBaseMovimientoInventario(MovimientoInventarioDto dto) {
        ValidationUtil.isRequired(dto.getProducto(), "El producto es requerido.");
        ValidationUtil.isRequired(dto.getUbicacion(), "La ubicación es requerida.");
        ValidationUtil.isRequired(dto.getTipoMovimiento(), "El tipo de movimiento es requerido.");
        ValidationUtil.isRequired(dto.getCantidad(), "La cantidad es requerida.");

        boolean isValid = Arrays.stream(EnumCodigoEstado.values())
            .anyMatch(e -> e.name().equalsIgnoreCase(dto.getTipoMovimiento()) || e.getCodigo().equalsIgnoreCase(dto.getTipoMovimiento()));
            
        if (!isValid) {
             throw new ValidationException("Tipo de movimiento no válido. Valores permitidos: " + 
                 Arrays.stream(EnumCodigoEstado.values())
                       .map(EnumCodigoEstado::getCodigo)
                       .collect(java.util.stream.Collectors.joining(", ")));
        }
    }
}
