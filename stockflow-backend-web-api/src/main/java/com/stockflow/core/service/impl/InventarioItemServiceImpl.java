package com.stockflow.core.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stockflow.core.dto.InventarioItemDto;
import com.stockflow.core.entity.InventarioItem;
import com.stockflow.core.entity.Producto;
import com.stockflow.core.entity.Ubicacion;
import com.stockflow.core.exception.ConflictException;
import com.stockflow.core.exception.ValidationException;
import com.stockflow.core.repository.InventarioItemRepository;
import com.stockflow.core.repository.ProductoRepository;
import com.stockflow.core.repository.UbicacionRepository;
import com.stockflow.core.service.InventarioItemService;
import com.stockflow.core.utils.ValidationUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventarioItemServiceImpl implements InventarioItemService {

    private final InventarioItemRepository inventarioItemRepository;
    private final ProductoRepository productoRepository;
    private final UbicacionRepository ubicacionRepository;

    @Override
    @Transactional
    public InventarioItemDto insert(InventarioItemDto dto) {
        ValidationUtil.isRequired(dto.getProducto(), "El producto es requerido.");
        ValidationUtil.isRequired(dto.getUbicacion(), "La ubicación es requerida.");
        ValidationUtil.isRequired(dto.getCantidad(), "La cantidad es requerida.");

        if (dto.getCantidad() < 0) {
            throw new ValidationException("La cantidad no puede ser negativa.");
        }

        // Validar existencias
        Producto producto = productoRepository.findById(dto.getProducto().getId())
                .orElseThrow(() -> new ValidationException("Producto no encontrado"));
        Ubicacion ubicacion = ubicacionRepository.findById(dto.getUbicacion().getId())
                .orElseThrow(() -> new ValidationException("Ubicación no encontrada"));

        // Validar duplicado (Producto + Ubicacion + Lote)
        List<InventarioItem> existing = inventarioItemRepository.findByProductoIdAndUbicacionIdAndLote(
                producto.getId(), ubicacion.getId(), dto.getLote());
        
        if (!existing.isEmpty()) {
             // Si el lote es nulo, la consulta anterior busca por null. Si encuentra algo, es duplicado.
             throw new ConflictException("Ya existe un registro de inventario para este producto en esta ubicación (y lote).",null);
        }
        
        // Verificar si existe registro sin lote si el lote es nulo (aunque la query anterior ya lo cubre en teoria, JPA a veces es tricky con nulls)
        // Pero findBy...AndLote maneja null si se pasa null? Depende del driver.
        // Mejor ser explícito:
        if (dto.getLote() == null) {
             List<InventarioItem> sameProductLocation = inventarioItemRepository.findByProductoIdAndUbicacionId(producto.getId(), ubicacion.getId());
             // Filtrar en memoria los que tienen lote null
             boolean duplicate = sameProductLocation.stream().anyMatch(i -> i.getLote() == null);
             if (duplicate) {
                 throw new ConflictException(
                         "Ya existe un registro de inventario para este producto en esta ubicación sin lote.", null);
             }
         }

        InventarioItem entity = dto.toEntity();
        // Asegurar que es un registro nuevo y no un update accidental por ID residual
        entity.setId(null); 
        entity.setProducto(producto);
        entity.setUbicacion(ubicacion);
        
        if (entity.getFechaUltimoConteo() == null) {
            entity.setFechaUltimoConteo(LocalDateTime.now());
        }

        InventarioItem saved = inventarioItemRepository.save(entity);
        return InventarioItemDto.build().fromEntity(saved);
    }

    @Override
    @Transactional
    public InventarioItemDto update(InventarioItemDto dto) {
        InventarioItem oldEntity = inventarioItemRepository.findById(dto.getId())
                .orElseThrow(() -> new ValidationException("Item de inventario no encontrado"));

        // Si cambia producto o ubicación, validar que no choque con otro
        boolean checkConflict = false;
        Integer newProdId = oldEntity.getProducto().getId();
        Integer newUbiId = oldEntity.getUbicacion().getId();
        String newLote = oldEntity.getLote();

        if (dto.getProducto() != null && dto.getProducto().getId() != null) {
            newProdId = dto.getProducto().getId();
            checkConflict = true;
        }
        if (dto.getUbicacion() != null && dto.getUbicacion().getId() != null) {
            newUbiId = dto.getUbicacion().getId();
            checkConflict = true;
        }
        if (dto.getLote() != null && !Objects.equals(dto.getLote(), oldEntity.getLote())) {
            newLote = dto.getLote();
            checkConflict = true;
        }

        if (checkConflict) {
             // Verificar si ya existe otro registro con la nueva combinación
             // Excluyendo el actual
             List<InventarioItem> potentialConflicts = inventarioItemRepository.findByProductoIdAndUbicacionIdAndLote(newProdId, newUbiId, newLote);
             for (InventarioItem conflict : potentialConflicts) {
                 if (!conflict.getId().equals(oldEntity.getId())) {
                     throw new ConflictException("La modificación genera un conflicto con otro registro de inventario existente.", null);
                 }
             }
             
             // Doble check para nulos si el lote es nulo
             if (newLote == null) {
                 List<InventarioItem> sameLoc = inventarioItemRepository.findByProductoIdAndUbicacionId(newProdId, newUbiId);
                 boolean conflict = sameLoc.stream()
                         .anyMatch(i -> i.getLote() == null && !i.getId().equals(oldEntity.getId()));
                 if (conflict) {
                     throw new ConflictException("La modificación genera un conflicto con otro registro de inventario existente (sin lote).", null);
                 }
             }
        }

        // Actualizar relaciones si cambiaron
        if (dto.getProducto() != null && dto.getProducto().getId() != null) {
             oldEntity.setProducto(productoRepository.findById(dto.getProducto().getId())
                     .orElseThrow(() -> new ValidationException("Producto no encontrado")));
        }
        if (dto.getUbicacion() != null && dto.getUbicacion().getId() != null) {
             oldEntity.setUbicacion(ubicacionRepository.findById(dto.getUbicacion().getId())
                     .orElseThrow(() -> new ValidationException("Ubicación no encontrada")));
        }

        // Actualizar campos simples
        if (dto.getCantidad() != null) {
            if (dto.getCantidad() < 0) throw new ValidationException("La cantidad no puede ser negativa.");
            oldEntity.setCantidad(dto.getCantidad());
        }
        if (dto.getCantidadReservada() != null) {
            if (dto.getCantidadReservada() < 0) throw new ValidationException("La cantidad reservada no puede ser negativa.");
            oldEntity.setCantidadReservada(dto.getCantidadReservada());
        }
        if (dto.getLote() != null) oldEntity.setLote(dto.getLote());
        if (dto.getFechaVencimiento() != null) oldEntity.setFechaVencimiento(dto.getFechaVencimiento());
        if (dto.getFechaUltimoConteo() != null) oldEntity.setFechaUltimoConteo(dto.getFechaUltimoConteo());
        
        InventarioItem saved = inventarioItemRepository.save(oldEntity);
        return InventarioItemDto.build().fromEntity(saved);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        InventarioItem entity = inventarioItemRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Item de inventario no encontrado"));
        
        if (entity.getCantidad() > 0) {
            // Opcional: Impedir borrar si hay stock. 
            // Pero como es un CRUD administrativo, quizás se permita.
            // Dejaré que se borre, asumiendo que es un ajuste manual fuerte.
        }
        inventarioItemRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioItemDto> findAll() {
        return inventarioItemRepository.findAll().stream()
                .map(e -> InventarioItemDto.build().fromEntity(e))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InventarioItemDto findById(Integer id) {
        return inventarioItemRepository.findById(id)
                .map(e -> InventarioItemDto.build().fromEntity(e))
                .orElseThrow(() -> new ValidationException("Item de inventario no encontrado"));
    }
}
