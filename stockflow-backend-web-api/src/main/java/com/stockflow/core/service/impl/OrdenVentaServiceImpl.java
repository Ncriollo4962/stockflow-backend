package com.stockflow.core.service.impl;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.stockflow.core.dto.DetalleOrdenVentaDto;
import com.stockflow.core.dto.OrdenVentaDto;
import com.stockflow.core.entity.DetalleOrdenVenta;
import com.stockflow.core.entity.OrdenVenta;
import com.stockflow.core.enums.EnumCodigoEstado;
import com.stockflow.core.exception.ConflictException;
import com.stockflow.core.exception.ValidationException;
import com.stockflow.core.repository.DetalleOrdenVentaRepository;
import com.stockflow.core.repository.OrdenVentaRepository;
import com.stockflow.core.service.OrdenVentaService;
import com.stockflow.core.utils.ValidationUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrdenVentaServiceImpl implements OrdenVentaService {

    private final OrdenVentaRepository ordenVentaRepository;
    private final DetalleOrdenVentaRepository detalleOrdenVentaRepository;

    @Override
    @Transactional
    public OrdenVentaDto insert(OrdenVentaDto dto) {
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
    @Transactional
    public OrdenVentaDto update(OrdenVentaDto dto) {
        validarCamposBaseOrdenVenta(dto);

        OrdenVenta ordenVentaBD = ordenVentaRepository
                .findById(Objects.requireNonNull(dto.getId(), "Orden de venta ID must not be null"))
                .orElseThrow(() -> new ValidationException("Orden de venta no encontrada con ID: " + dto.getId()));

        validateVersion(dto, ordenVentaBD);

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
    @Transactional
    public void delete(Integer d) {
        if (d != null) {
            ordenVentaRepository.deleteById(d);
        }
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
                        validarCamposBaseDetalleOrdenVenta(DetalleOrdenVentaDto.build().fromEntity(d));
                        d.setOrdenVenta(savedOrdenVenta);
                        d.setProducto(d.getProducto());
                        return d;
                    })
                    .toList();

            detalleOrdenVentaRepository.saveAll(Objects.requireNonNull(detallesToSave, "Detalles de la orden de venta no pueden ser nulos"));
        }
    }

    private static void validarCamposBaseDetalleOrdenVenta(DetalleOrdenVentaDto dto) {
        ValidationUtil.isRequired(dto.getProducto(), "El producto es requerido.");
        ValidationUtil.isRequired(dto.getCantidad(), "La cantidad es requerida.");
        ValidationUtil.isRequired(dto.getPrecioUnitario(), "El precio unitario es requerido.");
    }
}
