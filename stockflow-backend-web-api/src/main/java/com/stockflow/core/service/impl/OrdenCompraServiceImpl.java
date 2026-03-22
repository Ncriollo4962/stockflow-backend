package com.stockflow.core.service.impl;

import java.time.Year;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.stockflow.core.dto.DetalleOrdenCompraDto;
import com.stockflow.core.dto.OrdenCompraDto;
import com.stockflow.core.entity.DetalleOrdenCompra;
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
import com.stockflow.core.service.OrdenCompraService;
import com.stockflow.core.utils.ValidationUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrdenCompraServiceImpl implements OrdenCompraService {

    private final OrdenCompraRepository ordenCompraRepository;
    private final DetalleOrdenCompraRepository detalleOrdenCompraRepository;
    private final ProveedorRepository proveedorRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public OrdenCompraDto insert(OrdenCompraDto dto) {
        validarCamposBaseOrdenCompra(dto);

        OrdenCompra ordenCompra = dto.toEntity();

        ordenCompra.setId(null);
        ordenCompra.setVersion(null);
        if (dto.getEstado() == null) {
            ordenCompra.setEstado(EnumCodigoEstado.APERTURADA.getCodigo());
        }
        // Asociar Proveedor existente con validación
        asignarProveedor(dto, ordenCompra);

        // Asociar Usuario existente con validación
        asignarUsuario(dto, ordenCompra);

        // Guardar la orden de compra
        OrdenCompra savedOrdenCompra = ordenCompraRepository.saveAndFlush(ordenCompra);

        // Guardar los detalles de la orden de compra
        saveDetallesOrdenCompra(dto, savedOrdenCompra);

        return OrdenCompraDto.build().fromEntity(savedOrdenCompra);
    }

    private void saveDetallesOrdenCompra(OrdenCompraDto dto, OrdenCompra savedOrdenCompra) {
        detalleOrdenCompraRepository.deleteByOrdenCompraId(savedOrdenCompra.getId());
        if (!CollectionUtils.isEmpty(dto.getDetallesOrdenCompra())) {
            List<DetalleOrdenCompra> detallesToSave = dto.getDetallesOrdenCompra().stream()
                    .map(DetalleOrdenCompraDto::toEntity)
                    .map(d -> {
                        d.setId(null);
                        validarCamposBaseDetalleOrdenCompra(DetalleOrdenCompraDto.build().fromEntity(d));
                        d.setOrdenCompra(savedOrdenCompra);
                        d.setProducto(d.getProducto());
                        return d;
                    })
                    .toList();

            detalleOrdenCompraRepository.saveAll(Objects.requireNonNull(detallesToSave, "Los detalles de la orden de compra no pueden ser nulos"));
        }
    }

    private void asignarUsuario(OrdenCompraDto dto, OrdenCompra ordenCompra) {
        if (dto.getUsuario() != null && dto.getUsuario().getId() != null) {
            Usuario usuario = usuarioRepository.findById(Objects.requireNonNull(dto.getUsuario().getId(), "Usuario ID must not be null"))
                    .orElseThrow(
                            () -> new ValidationException("Usuario no encontrado con ID: " + dto.getUsuario().getId()));
            ordenCompra.setUsuario(usuario);
        }
    }

    private void asignarProveedor(OrdenCompraDto dto, OrdenCompra ordenCompra) {
        if (dto.getProveedor() != null && dto.getProveedor().getId() != null) {
            Proveedor proveedor = proveedorRepository.findById(Objects.requireNonNull(dto.getProveedor().getId(), "Proveedor ID must not be null"))
                    .orElseThrow(() -> new ValidationException(
                            "Proveedor no encontrado con ID: " + dto.getProveedor().getId()));
            ordenCompra.setProveedor(proveedor);
        }
    }

    @Override
    @Transactional
    public OrdenCompraDto update(OrdenCompraDto dto) {
        validarCamposBaseOrdenCompra(dto);

        OrdenCompra ordenCompraBD = ordenCompraRepository.findById(Objects.requireNonNull(dto.getId(), "Orden de compra ID must not be null"))
                .orElseThrow(() -> new ValidationException("Orden de compra no encontrada con ID: " + dto.getId()));

        validateVersion(dto, ordenCompraBD);

        ordenCompraBD.setNumeroOrden(dto.getNumeroOrden());
        ordenCompraBD.setFechaOrdenCompra(dto.getFechaOrdenCompra());
        ordenCompraBD.setFechaEntrega(dto.getFechaEntrega());
        ordenCompraBD.setNotas(dto.getNotas());
        ordenCompraBD.setEstado(dto.getEstado());
        ordenCompraBD.setTotalCompra(dto.getTotalCompra());
        // Asociar Proveedor existente con validación
        asignarProveedor(dto, ordenCompraBD);

        // Asociar Usuario existente con validación
        asignarUsuario(dto, ordenCompraBD);

        OrdenCompra savedOrdenCompra = ordenCompraRepository.saveAndFlush(ordenCompraBD);
        // Guardar los detalles de la orden de compra
        saveDetallesOrdenCompra(dto, savedOrdenCompra);

        return OrdenCompraDto.build().fromEntity(savedOrdenCompra);
    }

    @Override
    @Transactional
    public void delete(Integer d) {
        if (d != null) {
            detalleOrdenCompraRepository.deleteByOrdenCompraId(d);
            
            OrdenCompra ordenCompra = ordenCompraRepository.findById(d)
                    .orElseThrow(() -> new ValidationException("Orden de compra no encontrada"));
            ordenCompra.setEstado(EnumCodigoEstado.ANULADA.getCodigo());
            ordenCompraRepository.save(ordenCompra);
        }
    }

    @Override
    @Transactional
    public void deleteAll(List<Integer> ids) {
        if (ids != null) {
            ids.forEach(this::delete);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenCompraDto> findPendientesRecepcion() {
        List<String> estados = List.of(
            EnumCodigoEstado.PENDIENTE_RECEPCION.getCodigo()
        );
        OrdenCompraDto template = OrdenCompraDto.build();
        return ordenCompraRepository.findByEstadoIn(estados).stream()
                .map(orden -> OrdenCompraDto.build().fromEntity(template, orden))
                .toList();
    }

    @Override
    @Transactional
    public OrdenCompraDto cambiarEstado(Integer id, String estado) {
        ValidationUtil.isRequired(id, "El ID de orden de compra es requerido.");
        ValidationUtil.isRequired(estado, "El estado es requerido.");

        OrdenCompra ordenCompra = ordenCompraRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Orden de compra no encontrada con ID: " + id));

        ordenCompra.setEstado(estado);
        OrdenCompra saved = ordenCompraRepository.saveAndFlush(ordenCompra);
        return OrdenCompraDto.build().fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenCompraDto> findAll() {

        OrdenCompraDto template = OrdenCompraDto.build();
        return ordenCompraRepository.findAll().stream()
                .map(orden -> OrdenCompraDto.build().fromEntity(template, orden))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrdenCompraDto findById(Integer id) {
        return ordenCompraRepository.findById(Objects.requireNonNull(id, "Orden de compra ID must not be null"))
                .map(orden -> OrdenCompraDto.build().fromEntity(orden))
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public String generateNumeroOrden() {
        String year = String.valueOf(Year.now().getValue());
        String prefix = "OC-" + year + "-";

        return ordenCompraRepository.findTopByOrderByIdDesc()
                .map(OrdenCompra::getNumeroOrden)
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

    private static void validarCamposBaseOrdenCompra(OrdenCompraDto dto) {
        ValidationUtil.isRequired(dto.getNumeroOrden(), "El número de orden es obligatorio.");
        ValidationUtil.isRequired(dto.getProveedor(), "El proveedor es requerido.");
        ValidationUtil.isRequired(dto.getProveedor().getId(), "El ID de proveedor es requerido.");
        ValidationUtil.isRequired(dto.getUsuario(), "El usuario es requerido.");
        ValidationUtil.isRequired(dto.getUsuario().getId(), "El ID de usuario es requerido.");
        ValidationUtil.isRequired(dto.getFechaOrdenCompra(), "La fecha de orden de compra es requerida.");
        ValidationUtil.isRequired(dto.getEstado(), "El estado es requerido.");
        ValidationUtil.isRequired(dto.getTotalCompra(), "El total de compra es requerido.");

    }

    private static void validarCamposBaseDetalleOrdenCompra(DetalleOrdenCompraDto dto) {
        ValidationUtil.isRequired(dto.getProducto(), "El producto es requerido.");
        ValidationUtil.isRequired(dto.getProducto().getId(), "El ID de producto es requerido.");
        ValidationUtil.isRequired(dto.getCantidad(), "La cantidad es requerida.");
        ValidationUtil.isRequired(dto.getPrecioUnitario(), "El precio unitario es requerido.");
    }

    private static void validateVersion(OrdenCompraDto ordenCompraDto, OrdenCompra ordenCompraBD) {
        if (ordenCompraDto.getVersion() != null && !ordenCompraBD.getVersion().equals(ordenCompraDto.getVersion())) {
            OrdenCompraDto actual = OrdenCompraDto.build().fromEntity(new OrdenCompraDto(), ordenCompraBD);
            throw new ConflictException(
                    "La orden de compra ha sido modificado por otro administrador. Versión enviada: "
                            + ordenCompraDto.getVersion() + ", Versión actual BD: " + ordenCompraBD.getVersion(),
                    actual);
        }
    }
}
