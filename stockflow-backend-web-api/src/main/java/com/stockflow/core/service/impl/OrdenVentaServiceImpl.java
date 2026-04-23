package com.stockflow.core.service.impl;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.stockflow.core.config.ApplicationConfig.DeadlockRetryExecutor;
import com.stockflow.core.dto.DetalleOrdenVentaDto;
import com.stockflow.core.dto.OrdenVentaDto;
import com.stockflow.core.entity.DetalleOrdenVenta;
import com.stockflow.core.entity.InventarioItem;
import com.stockflow.core.entity.OrdenVenta;
import com.stockflow.core.enums.EnumCodigoEstado;
import com.stockflow.core.exception.ConflictException;
import com.stockflow.core.exception.ValidationException;
import com.stockflow.core.repository.DetalleOrdenVentaRepository;
import com.stockflow.core.repository.InventarioItemRepository;
import com.stockflow.core.repository.OrdenVentaRepository;
import com.stockflow.core.service.OrdenVentaService;
import com.stockflow.core.utils.ValidationUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrdenVentaServiceImpl implements OrdenVentaService {

    private final OrdenVentaRepository ordenVentaRepository;
    private final DetalleOrdenVentaRepository detalleOrdenVentaRepository;
    private final InventarioItemRepository inventarioItemRepository;
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
    public OrdenVentaDto insert(OrdenVentaDto dto) {
        return executeWithRetry(() -> insertInternal(dto));
    }

    @Transactional
    protected OrdenVentaDto insertInternal(OrdenVentaDto dto) {
        validarCamposBaseOrdenVenta(dto);

        OrdenVenta ordenVenta = dto.toEntity();

        ordenVenta.setId(null);
        ordenVenta.setVersion(null);
        if (dto.getEstado() == null) {
            ordenVenta.setEstado(EnumCodigoEstado.APERTURADA.getCodigo());
        }

        if (ordenVenta.getTotalVenta() == null) {
            ordenVenta.setTotalVenta(BigDecimal.ZERO);
        }
        
        OrdenVenta savedOrdenVenta = ordenVentaRepository.save(ordenVenta);

        // Guardar los detalles de la orden de venta
        saveDetallesOrdenVenta(dto, savedOrdenVenta);

        return OrdenVentaDto.build().fromEntity(savedOrdenVenta);
    }


    @Override
    public OrdenVentaDto update(OrdenVentaDto dto) {
        return executeWithRetry(() -> updateInternal(dto));
    }

    @Transactional
    protected OrdenVentaDto updateInternal(OrdenVentaDto dto) {
        validarCamposBaseOrdenVenta(dto);

        OrdenVenta ordenVentaBD = ordenVentaRepository
                .findById(Objects.requireNonNull(dto.getId(), "Orden de venta ID must not be null"))
                .orElseThrow(() -> new ValidationException("Orden de venta no encontrada con ID: " + dto.getId()));

        validateVersion(dto, ordenVentaBD);

        // Revertir stock de los detalles actuales antes de eliminarlos/actualizarlos
        List<DetalleOrdenVenta> oldDetalles = detalleOrdenVentaRepository.findByOrdenVentaId(ordenVentaBD.getId());
        for (DetalleOrdenVenta old : oldDetalles) {
            updateStock(old, true); // Revertir (devolver stock, quitar reserva)
        }

        ordenVentaBD.setNumeroOrden(dto.getNumeroOrden());
        ordenVentaBD.setClienteNombre(dto.getClienteNombre());
        ordenVentaBD.setClienteEmail(dto.getClienteEmail());
        ordenVentaBD.setClienteTelefono(dto.getClienteTelefono());
        ordenVentaBD.setDireccion(dto.getDireccion());
        ordenVentaBD.setFechaVenta(dto.getFechaVenta());
        ordenVentaBD.setEstado(dto.getEstado());
        ordenVentaBD.setTotalVenta(dto.getTotalVenta());


        OrdenVenta savedOrdenVenta = ordenVentaRepository.saveAndFlush(ordenVentaBD);
        // Guardar los detalles de la orden de venta
        saveDetallesOrdenVenta(dto, savedOrdenVenta);

        return OrdenVentaDto.build().fromEntity(savedOrdenVenta);
    }

    @Override
    public void delete(Integer d) {
        runWithRetry(() -> deleteInternal(d));
    }

    @Transactional
    protected void deleteInternal(Integer d) {
        if (d != null) {
            List<DetalleOrdenVenta> oldDetalles = detalleOrdenVentaRepository.findByOrdenVentaId(d);
            for (DetalleOrdenVenta old : oldDetalles) {
                updateStock(old, true); // Revertir stock al eliminar
            }
            detalleOrdenVentaRepository.deleteByOrdenVentaId(d);
            
            OrdenVenta ordenVenta = ordenVentaRepository.findById(d)
                    .orElseThrow(() -> new ValidationException("Orden de venta no encontrada"));
            ordenVenta.setEstado(EnumCodigoEstado.ANULADA.getCodigo());
            ordenVentaRepository.save(ordenVenta);
        }
    }

    @Override
    public void deleteAll(List<Integer> ids) {
        if (ids != null) {
            ids.forEach(this::delete);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenVentaDto> findPendientesDespacho() {
        List<String> estados = List.of(
            EnumCodigoEstado.PENDIENTE_DESPACHO.getCodigo()
        );
        OrdenVentaDto template = OrdenVentaDto.build();
        return ordenVentaRepository.findByEstadoIn(estados).stream()
                .map(orden -> OrdenVentaDto.build().fromEntity(template, orden))
                .toList();
    }

    @Override
    @Transactional
    public OrdenVentaDto cambiarEstado(Integer id, String estado) {
        ValidationUtil.isRequired(id, "El ID de orden de venta es requerido.");
        ValidationUtil.isRequired(estado, "El estado es requerido.");

        OrdenVenta ordenVenta = ordenVentaRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Orden de venta no encontrada con ID: " + id));

        ordenVenta.setEstado(estado);
        OrdenVenta saved = ordenVentaRepository.saveAndFlush(ordenVenta);
        return OrdenVentaDto.build().fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenVentaDto> findAll() {
        OrdenVentaDto template = OrdenVentaDto.build();
        return ordenVentaRepository.findAll().stream()
                .map(orden -> OrdenVentaDto.build().fromEntity(template, orden))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrdenVentaDto findById(Integer id) {
        return ordenVentaRepository.findById(Objects.requireNonNull(id, "Orden de venta ID must not be null"))
                .map(orden -> OrdenVentaDto.build().fromEntity(orden))
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public String generateNumeroOrden() {
        String year = String.valueOf(Year.now().getValue());
        String prefix = "OV-" + year + "-";

        return ordenVentaRepository.findTopByOrderByIdDesc()
                .map(OrdenVenta::getNumeroOrden)
                .filter(last -> last != null && last.startsWith(prefix))
                .map(last -> {
                    try {
                        String[] parts = last.split("-");
                        long correlativo = Long.parseLong(parts[2]);
                        return String.format("%s%06d", prefix, correlativo + 1);
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                        return prefix + "000001";
                    }
                })
                .orElse(prefix + "000001");
    }

    private static void validarCamposBaseOrdenVenta(OrdenVentaDto dto) {
        ValidationUtil.isRequired(dto.getNumeroOrden(), "El número de orden es obligatorio.");
        ValidationUtil.isRequired(dto.getUsuario(), "El usuario es requerido.");
        ValidationUtil.isRequired(dto.getUsuario().getId(), "El ID de usuario es requerido.");
        ValidationUtil.isRequired(dto.getClienteNombre(), "El nombre del cliente es requerido.");
        ValidationUtil.isRequired(dto.getClienteTelefono(), "El teléfono del cliente es requerido.");
        ValidationUtil.isRequired(dto.getDireccion(), "La dirección es requerida.");
        ValidationUtil.isRequired(dto.getEstado(), "El estado es requerido.");
    }

    private void validateVersion(OrdenVentaDto dto, OrdenVenta entity) {
        if (dto.getVersion() != null && !dto.getVersion().equals(entity.getVersion())) {
            OrdenVentaDto actual = OrdenVentaDto.build().fromEntity(new OrdenVentaDto(), entity);
            throw new ConflictException(
                    "La orden de venta ha sido modificado por otro administrador. Versión enviada: "
                            + dto.getVersion() + ", Versión actual BD: " + entity.getVersion(),
                    actual);
        }
    }

    private void saveDetallesOrdenVenta(OrdenVentaDto dto, OrdenVenta savedOrdenVenta) {
        detalleOrdenVentaRepository.deleteByOrdenVentaId(savedOrdenVenta.getId());
        if (!CollectionUtils.isEmpty(dto.getDetallesOrdenVenta())) {
            List<DetalleOrdenVenta> detallesToSave = dto.getDetallesOrdenVenta().stream()
                    .map(DetalleOrdenVentaDto::toEntity)
                    .map(d -> {
                        d.setId(null); // Force ID to null to create new records after deletion
                        validarCamposBaseDetalleOrdenVenta(DetalleOrdenVentaDto.build().fromEntity(d));
                        d.setOrdenVenta(savedOrdenVenta);
                        d.setProducto(d.getProducto());
                        
                        // Actualizar stock (reservar)
                        updateStock(d, false);

                        return d;
                    })
                    .toList();

            detalleOrdenVentaRepository.saveAll(Objects.requireNonNull(detallesToSave, "Detalles de la orden de venta no pueden ser nulos"));
        }
    }

    private void updateStock(DetalleOrdenVenta det, boolean isRevert) {
        List<InventarioItem> items = inventarioItemRepository.findForUpdateByProductoId(det.getProducto().getId());
        
        if (items.isEmpty()) {
            if (isRevert) return; // Nada que revertir si no hay inventario (raro pero posible)
            throw new ValidationException("No hay stock disponible para el producto: " + det.getProducto().getNombre());
        }

        int qtyNeeded = det.getCantidad() == null ? 0 : det.getCantidad();
        if (qtyNeeded <= 0) {
            return;
        }

        if (isRevert) {
            int qtyToRelease = qtyNeeded;
            for (InventarioItem item : items) {
                if (qtyToRelease <= 0) {
                    break;
                }

                int reservada = item.getCantidadReservada() == null ? 0 : item.getCantidadReservada();
                if (reservada <= 0) {
                    continue;
                }

                int toRelease = Math.min(reservada, qtyToRelease);
                item.setCantidadReservada(reservada - toRelease);
                inventarioItemRepository.save(item);
                qtyToRelease -= toRelease;
            }

            if (qtyToRelease > 0) {
                throw new ValidationException("No se puede liberar más de lo reservado. Faltan por liberar: " + qtyToRelease);
            }
            return;
        }

        for (InventarioItem item : items) {
            if (qtyNeeded <= 0) break;

            int cantidad = item.getCantidad() == null ? 0 : item.getCantidad();
            int reservada = item.getCantidadReservada() == null ? 0 : item.getCantidadReservada();
            if (reservada > cantidad) {
                throw new ValidationException("Inventario inconsistente: la cantidad reservada es mayor que el stock para el item ID " + item.getId());
            }

            int disponible = cantidad - reservada;
            if (disponible <= 0) {
                continue;
            }

            int toReserve = Math.min(disponible, qtyNeeded);
            item.setCantidadReservada(reservada + toReserve);
            inventarioItemRepository.save(item);
            
            qtyNeeded -= toReserve;
        }

        if (qtyNeeded > 0) {
            throw new ValidationException("Stock insuficiente para el producto: " + det.getProducto().getNombre() + ". Faltan: " + qtyNeeded);
        }
    }

    private static void validarCamposBaseDetalleOrdenVenta(DetalleOrdenVentaDto dto) {
        ValidationUtil.isRequired(dto.getProducto(), "El producto es requerido.");
        ValidationUtil.isRequired(dto.getCantidad(), "La cantidad es requerida.");
        ValidationUtil.isRequired(dto.getPrecioUnitario(), "El precio unitario es requerido.");
    }
}
