package com.stockflow.core.service.impl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stockflow.core.config.ApplicationConfig.DeadlockRetryExecutor;
import com.stockflow.core.dto.MovimientoInventarioDto;
import com.stockflow.core.entity.InventarioItem;
import com.stockflow.core.entity.DetalleOrdenCompra;
import com.stockflow.core.entity.DetalleOrdenVenta;
import com.stockflow.core.entity.MovimientoInventario;
import com.stockflow.core.entity.OrdenCompra;
import com.stockflow.core.entity.OrdenVenta;
import com.stockflow.core.entity.Producto;
import com.stockflow.core.entity.Ubicacion;
import com.stockflow.core.entity.Usuario;
import com.stockflow.core.enums.EnumCodigoEstado;
import com.stockflow.core.exception.ValidationException;
import com.stockflow.core.repository.DetalleOrdenCompraRepository;
import com.stockflow.core.repository.DetalleOrdenVentaRepository;
import com.stockflow.core.repository.InventarioItemRepository;
import com.stockflow.core.repository.MovimientoInventarioRepository;
import com.stockflow.core.repository.OrdenCompraRepository;
import com.stockflow.core.repository.OrdenVentaRepository;
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
    private final OrdenCompraRepository ordenCompraRepository;
    private final DetalleOrdenCompraRepository detalleOrdenCompraRepository;
    private final OrdenVentaRepository ordenVentaRepository;
    private final DetalleOrdenVentaRepository detalleOrdenVentaRepository;
    private final DeadlockRetryExecutor deadlockRetryExecutor;

    private <T> T executeWithRetry(Supplier<T> action) {
        DeadlockRetryExecutor executor = deadlockRetryExecutor;
        return executor != null ? executor.execute(action) : action.get();
    }

    private void runWithRetry(Runnable action) {
        DeadlockRetryExecutor executor = deadlockRetryExecutor;
        if (executor != null) {
            executor.run(action);
            return;
        }
        action.run();
    }

    @Override
    public List<MovimientoInventarioDto> insertAll(List<MovimientoInventarioDto> dtos) {
        return executeWithRetry(() -> insertAllInternal(dtos));
    }

    @Override
    public MovimientoInventarioDto insert(MovimientoInventarioDto dto) {
        return executeWithRetry(() -> insertInternal(dto));
    }

    @Override
    public MovimientoInventarioDto update(MovimientoInventarioDto dto) {
        return executeWithRetry(() -> updateInternal(dto));
    }

    @Override
    public void delete(Integer id) {
        runWithRetry(() -> deleteInternal(id));
    }

    @Transactional
    protected List<MovimientoInventarioDto> insertAllInternal(List<MovimientoInventarioDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            throw new ValidationException("La lista de movimientos no puede estar vacía.");
        }
        return dtos.stream().map(this::insertInternal).toList();
    }

    @Transactional
    protected MovimientoInventarioDto insertInternal(MovimientoInventarioDto dto) {
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
        updateOrdenesRelacionadas(saved, false);

        return MovimientoInventarioDto.build().fromEntity(saved);
    }

    @Transactional
    protected MovimientoInventarioDto updateInternal(MovimientoInventarioDto dto) {
        MovimientoInventario oldEntity = movimientoInventarioRepository.findById(dto.getId())
                .orElseThrow(() -> new ValidationException("Movimiento no encontrado"));

        // Revertir efecto anterior
        updateOrdenesRelacionadas(oldEntity, true);
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
        updateOrdenesRelacionadas(oldEntity, false);

        MovimientoInventario saved = movimientoInventarioRepository.save(oldEntity);
        return MovimientoInventarioDto.build().fromEntity(saved);
    }

    @Transactional
    protected void deleteInternal(Integer id) {
        MovimientoInventario entity = movimientoInventarioRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Movimiento no encontrado"));

        updateOrdenesRelacionadas(entity, true);
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
        int qty = mov.getCantidad() == null ? 0 : mov.getCantidad();
        int multiplier = 0;
        String tipo = mov.getTipoMovimiento();
        boolean isAjusteConteo = !isRevert && isAjusteInventarioMensual(tipo);
        LocalDateTime fechaConteo = isAjusteConteo ? LocalDateTime.now() : null;

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

        if (multiplier < 0 && isSalidaRegular(tipo) && isDespachoDeOrdenVenta(mov)) {
            updateInventarioDespachoOrdenVenta(mov, isRevert);
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

        if (change > 0) {
            InventarioItem itemToUpdate;
            if (items.isEmpty()) {
                itemToUpdate = InventarioItem.builder()
                        .producto(mov.getProducto())
                        .ubicacion(mov.getUbicacion())
                        .cantidad(0)
                        .cantidadReservada(0)
                        .build();
            } else {
                itemToUpdate = items.get(0);
            }

            int cantidadActual = itemToUpdate.getCantidad() == null ? 0 : itemToUpdate.getCantidad();
            int nuevaCantidad = cantidadActual + change;
            if (nuevaCantidad < 0) {
                throw new ValidationException("La cantidad no puede ser negativa.");
            }
            itemToUpdate.setCantidad(nuevaCantidad);
            if (isAjusteConteo) {
                itemToUpdate.setFechaUltimoConteo(fechaConteo);
            }
            inventarioItemRepository.save(itemToUpdate);
            return;
        }

        if (items.isEmpty()) {
            throw new ValidationException("No existe inventario para descontar stock en esta ubicación.");
        }

        for (InventarioItem item : items) {
            int cantidad = item.getCantidad() == null ? 0 : item.getCantidad();
            int reservada = item.getCantidadReservada() == null ? 0 : item.getCantidadReservada();
            if (reservada > cantidad) {
                throw new ValidationException("Inventario inconsistente: la cantidad reservada es mayor que el stock para el item ID " + item.getId());
            }
        }

        int qtyToDeduct = Math.abs(change);

        for (InventarioItem item : items) {
            if (qtyToDeduct <= 0) {
                break;
            }

            int reservada = item.getCantidadReservada() == null ? 0 : item.getCantidadReservada();
            if (reservada <= 0) {
                continue;
            }

            int toConsume = Math.min(reservada, qtyToDeduct);
            int nuevaReservada = reservada - toConsume;

            int cantidad = item.getCantidad() == null ? 0 : item.getCantidad();
            int nuevaCantidad = cantidad - toConsume;
            if (nuevaCantidad < 0) {
                throw new ValidationException("Stock insuficiente para descontar.");
            }
            if (nuevaCantidad < nuevaReservada) {
                throw new ValidationException("No se puede dejar el stock por debajo del reservado.");
            }

            item.setCantidad(nuevaCantidad);
            item.setCantidadReservada(nuevaReservada);
            if (isAjusteConteo) {
                item.setFechaUltimoConteo(fechaConteo);
            }
            inventarioItemRepository.save(item);

            qtyToDeduct -= toConsume;
        }

        for (InventarioItem item : items) {
            if (qtyToDeduct <= 0) {
                break;
            }

            int cantidad = item.getCantidad() == null ? 0 : item.getCantidad();
            int reservada = item.getCantidadReservada() == null ? 0 : item.getCantidadReservada();
            int disponible = cantidad - reservada;
            if (disponible <= 0) {
                continue;
            }

            int toDeduct = Math.min(disponible, qtyToDeduct);
            int nuevaCantidad = cantidad - toDeduct;
            if (nuevaCantidad < reservada) {
                throw new ValidationException("No se puede dejar el stock por debajo del reservado.");
            }

            item.setCantidad(nuevaCantidad);
            if (isAjusteConteo) {
                item.setFechaUltimoConteo(fechaConteo);
            }
            inventarioItemRepository.save(item);

            qtyToDeduct -= toDeduct;
        }

        if (qtyToDeduct > 0) {
            throw new ValidationException("Stock insuficiente para descontar. Faltan: " + qtyToDeduct);
        }
    }

    private static boolean isAjusteInventarioMensual(String tipo) {
        if (tipo == null) {
            return false;
        }
        return EnumCodigoEstado.AJUSTE_ENTRADA_INVENTARIO.name().equalsIgnoreCase(tipo)
                || EnumCodigoEstado.AJUSTE_ENTRADA_INVENTARIO.getCodigo().equalsIgnoreCase(tipo)
                || EnumCodigoEstado.AJUSTE_SALIDA_INVENTARIO.name().equalsIgnoreCase(tipo)
                || EnumCodigoEstado.AJUSTE_SALIDA_INVENTARIO.getCodigo().equalsIgnoreCase(tipo);
    }

    private boolean isDespachoDeOrdenVenta(MovimientoInventario mov) {
        if (mov == null) {
            return false;
        }
        if (mov.getReferencia() == null || mov.getReferencia().isBlank()) {
            return false;
        }
        if (mov.getProducto() == null || mov.getProducto().getId() == null) {
            return false;
        }

        Optional<OrdenVenta> ovOpt = ordenVentaRepository.findByNumeroOrden(mov.getReferencia().trim());
        if (ovOpt == null || ovOpt.isEmpty()) {
            return false;
        }

        OrdenVenta ov = ovOpt.get();
        List<DetalleOrdenVenta> detalles = detalleOrdenVentaRepository.findByOrdenVentaId(ov.getId());
        if (detalles == null || detalles.isEmpty()) {
            return false;
        }

        Integer productoId = mov.getProducto().getId();
        return detalles.stream().anyMatch(d -> d.getProducto() != null
                && d.getProducto().getId() != null
                && d.getProducto().getId().equals(productoId));
    }

    private void updateInventarioDespachoOrdenVenta(MovimientoInventario mov, boolean isRevert) {
        int qty = mov.getCantidad() == null ? 0 : mov.getCantidad();
        if (qty <= 0) {
            return;
        }

        List<InventarioItem> items = inventarioItemRepository.findForUpdateByProductoIdAndUbicacionId(
                mov.getProducto().getId(), mov.getUbicacion().getId());

        if (items.isEmpty()) {
            if (isRevert) {
                InventarioItem nuevo = InventarioItem.builder()
                        .producto(mov.getProducto())
                        .ubicacion(mov.getUbicacion())
                        .cantidad(qty)
                        .cantidadReservada(qty)
                        .build();
                inventarioItemRepository.save(nuevo);
                return;
            }
            throw new ValidationException("No existe inventario para descontar stock en esta ubicación.");
        }

        if (!isRevert) {
            int totalReservado = items.stream()
                    .mapToInt(i -> i.getCantidadReservada() == null ? 0 : i.getCantidadReservada())
                    .sum();
            if (totalReservado < qty) {
                throw new ValidationException("Reserva insuficiente para despachar en esta ubicación. Faltan: " + (qty - totalReservado));
            }
        }

        int qtyToApply = qty;
        for (InventarioItem item : items) {
            if (qtyToApply <= 0) {
                break;
            }

            int cantidad = item.getCantidad() == null ? 0 : item.getCantidad();
            int reservada = item.getCantidadReservada() == null ? 0 : item.getCantidadReservada();
            if (reservada > cantidad) {
                throw new ValidationException("Inventario inconsistente: la cantidad reservada es mayor que el stock para el item ID " + item.getId());
            }

            if (isRevert) {
                int nextCantidad = cantidad + qtyToApply;
                int nextReservada = reservada + qtyToApply;
                item.setCantidad(nextCantidad);
                item.setCantidadReservada(nextReservada);
                inventarioItemRepository.save(item);
                qtyToApply = 0;
                continue;
            }

            int toDispatch = Math.min(reservada, qtyToApply);
            if (toDispatch <= 0) {
                continue;
            }

            int nextCantidad = cantidad - toDispatch;
            int nextReservada = reservada - toDispatch;
            if (nextCantidad < 0) {
                throw new ValidationException("Stock insuficiente para descontar.");
            }
            if (nextReservada < 0) {
                throw new ValidationException("Reserva insuficiente para despachar en esta ubicación.");
            }
            if (nextReservada > nextCantidad) {
                throw new ValidationException("No se puede dejar el stock por debajo del reservado.");
            }

            item.setCantidad(nextCantidad);
            item.setCantidadReservada(nextReservada);
            inventarioItemRepository.save(item);

            qtyToApply -= toDispatch;
        }

        if (qtyToApply > 0) {
            if (isRevert) {
                throw new ValidationException("No se pudo revertir completamente el despacho. Faltan por revertir: " + qtyToApply);
            }
            throw new ValidationException("Reserva insuficiente para despachar en esta ubicación. Faltan: " + qtyToApply);
        }
    }

    private void updateOrdenesRelacionadas(MovimientoInventario mov, boolean isRevert) {
        if (mov == null) {
            return;
        }
        if (mov.getCantidad() == null || mov.getCantidad() == 0) {
            return;
        }
        if (mov.getProducto() == null || mov.getProducto().getId() == null) {
            return;
        }
        if (mov.getReferencia() == null || mov.getReferencia().isBlank()) {
            return;
        }

        String referencia = mov.getReferencia().trim();

        if (isAjusteInventarioMensual(mov.getTipoMovimiento())) {
            return;
        }

        if (isEntrada(mov.getTipoMovimiento())) {
            if (applyRecepcionOrdenCompra(referencia, mov, isRevert)) {
                return;
            }
        }

        if (isSalida(mov.getTipoMovimiento())) {
            applyDespachoOrdenVenta(referencia, mov, isRevert);
        }
    }

    private boolean applyRecepcionOrdenCompra(String referencia, MovimientoInventario mov, boolean isRevert) {
        Optional<OrdenCompra> ocOpt = ordenCompraRepository.findByNumeroOrden(referencia);
        if (ocOpt == null || ocOpt.isEmpty()) {
            return false;
        }

        OrdenCompra ordenCompra = ocOpt.get();
        List<DetalleOrdenCompra> detalles = detalleOrdenCompraRepository.findByOrdenCompraId(ordenCompra.getId());
        if (detalles == null || detalles.isEmpty()) {
            return true;
        }

        Integer productoId = mov.getProducto().getId();
        DetalleOrdenCompra detalle = detalles.stream()
                .filter(d -> d.getProducto() != null && d.getProducto().getId() != null && d.getProducto().getId().equals(productoId))
                .findFirst()
                .orElseThrow(() -> new ValidationException("El producto no pertenece a la orden de compra: " + referencia));

        int current = detalle.getCantidadRecibida() != null ? detalle.getCantidadRecibida() : 0;
        int delta = isRevert ? -mov.getCantidad() : mov.getCantidad();
        int next = current + delta;

        if (next < 0) {
            throw new ValidationException("Cantidad recibida no puede ser negativa para la orden: " + referencia);
        }
        if (detalle.getCantidad() != null && next > detalle.getCantidad()) {
            throw new ValidationException("La cantidad recibida excede la cantidad solicitada para la orden: " + referencia);
        }

        detalle.setCantidadRecibida(next);
        if (detalle.getCantidad() != null && next >= detalle.getCantidad()) {
            detalle.setEstadoDetalle(EnumCodigoEstado.RECIBIDA_COMPLETA.getCodigo());
        } else {
            detalle.setEstadoDetalle(EnumCodigoEstado.PENDIENTE_RECEPCION.getCodigo());
        }
        detalleOrdenCompraRepository.save(detalle);

        boolean allComplete = detalles.stream().allMatch(d -> {
            int recibida = d.getCantidadRecibida() != null ? d.getCantidadRecibida() : 0;
            return d.getCantidad() != null && recibida >= d.getCantidad();
        });
        ordenCompra.setEstado(allComplete ? EnumCodigoEstado.RECIBIDA_COMPLETA.getCodigo() : EnumCodigoEstado.PENDIENTE_RECEPCION.getCodigo());
        ordenCompraRepository.save(ordenCompra);
        return true;
    }

    private void applyDespachoOrdenVenta(String referencia, MovimientoInventario mov, boolean isRevert) {
        Optional<OrdenVenta> ovOpt = ordenVentaRepository.findByNumeroOrden(referencia);
        if (ovOpt == null || ovOpt.isEmpty()) {
            return;
        }

        OrdenVenta ordenVenta = ovOpt.get();
        List<DetalleOrdenVenta> detalles = detalleOrdenVentaRepository.findByOrdenVentaId(ordenVenta.getId());
        if (detalles == null || detalles.isEmpty()) {
            return;
        }

        Integer productoId = mov.getProducto().getId();
        DetalleOrdenVenta detalle = detalles.stream()
                .filter(d -> d.getProducto() != null && d.getProducto().getId() != null && d.getProducto().getId().equals(productoId))
                .findFirst()
                .orElseThrow(() -> new ValidationException("El producto no pertenece a la orden de venta: " + referencia));

        int current = detalle.getCantidadDespachada() != null ? detalle.getCantidadDespachada() : 0;
        int delta = isRevert ? -mov.getCantidad() : mov.getCantidad();
        int next = current + delta;

        if (next < 0) {
            throw new ValidationException("Cantidad despachada no puede ser negativa para la orden: " + referencia);
        }
        if (detalle.getCantidad() != null && next > detalle.getCantidad()) {
            throw new ValidationException("La cantidad despachada excede la cantidad solicitada para la orden: " + referencia);
        }

        detalle.setCantidadDespachada(next);
        if (detalle.getCantidad() != null && next >= detalle.getCantidad()) {
            detalle.setEstadoDetalle(EnumCodigoEstado.FINALIZADA.getCodigo());
        } else {
            detalle.setEstadoDetalle(EnumCodigoEstado.PENDIENTE_DESPACHO.getCodigo());
        }
        detalleOrdenVentaRepository.save(detalle);

        boolean allComplete = detalles.stream().allMatch(d -> {
            int despachada = d.getCantidadDespachada() != null ? d.getCantidadDespachada() : 0;
            return d.getCantidad() != null && despachada >= d.getCantidad();
        });
        ordenVenta.setEstado(allComplete ? EnumCodigoEstado.FINALIZADA.getCodigo() : EnumCodigoEstado.PENDIENTE_DESPACHO.getCodigo());
        ordenVentaRepository.save(ordenVenta);
    }

    private static boolean isEntrada(String tipo) {
        if (tipo == null) {
            return false;
        }
        return EnumCodigoEstado.ENTRADA.name().equalsIgnoreCase(tipo)
                || EnumCodigoEstado.ENTRADA.getCodigo().equalsIgnoreCase(tipo)
                || EnumCodigoEstado.AJUSTE_ENTRADA_INVENTARIO.name().equalsIgnoreCase(tipo)
                || EnumCodigoEstado.AJUSTE_ENTRADA_INVENTARIO.getCodigo().equalsIgnoreCase(tipo);
    }

    private static boolean isSalida(String tipo) {
        if (tipo == null) {
            return false;
        }
        return EnumCodigoEstado.SALIDA.name().equalsIgnoreCase(tipo)
                || EnumCodigoEstado.SALIDA.getCodigo().equalsIgnoreCase(tipo)
                || EnumCodigoEstado.AJUSTE_SALIDA_INVENTARIO.name().equalsIgnoreCase(tipo)
                || EnumCodigoEstado.AJUSTE_SALIDA_INVENTARIO.getCodigo().equalsIgnoreCase(tipo);
    }

    private static boolean isSalidaRegular(String tipo) {
        if (tipo == null) {
            return false;
        }
        return EnumCodigoEstado.SALIDA.name().equalsIgnoreCase(tipo)
                || EnumCodigoEstado.SALIDA.getCodigo().equalsIgnoreCase(tipo);
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
