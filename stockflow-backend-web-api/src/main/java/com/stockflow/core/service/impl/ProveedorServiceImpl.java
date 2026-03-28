package com.stockflow.core.service.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stockflow.core.dto.ProveedorDto;
import com.stockflow.core.entity.Proveedor;
import com.stockflow.core.exception.ConflictException;
import com.stockflow.core.exception.ValidationException;
import com.stockflow.core.repository.ProveedorRepository;
import com.stockflow.core.service.ProveedorService;
import com.stockflow.core.utils.ValidationUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorRepository proveedorRepository;

    @Override
    @Transactional
    public ProveedorDto insert(ProveedorDto d) {
        validarCamposBaseProveedor(d);

        if (proveedorRepository.findByCodigo(Objects.requireNonNull(d.getCodigo(), "codigo must not be null")).isPresent()) {
            throw new ConflictException("Ya existe un proveedor con el código: " + d.getCodigo(), null);
        }

        Proveedor entity = d.toEntity();
        entity.setId(null);
        entity.setVersion(null);
        if (entity.getEstado() == null) {
            entity.setEstado(true);
        }

        Proveedor saved = proveedorRepository.saveAndFlush(entity);
        return ProveedorDto.build().fromEntity(saved);
    }

    @Override
    @Transactional
    public ProveedorDto update(ProveedorDto d) {
        validarCamposBaseProveedor(d);

        Proveedor entity = proveedorRepository.findById(Objects.requireNonNull(d.getId(), "id must not be null"))
                .orElseThrow(() -> new ValidationException("Proveedor no encontrado con ID: " + d.getId()));

        validateVersion(d, entity);

        if (d.getCodigo() != null && !d.getCodigo().equals(entity.getCodigo())) {
            if (proveedorRepository.findByCodigo(d.getCodigo()).isPresent()) {
                throw new ConflictException("Ya existe un proveedor con el código: " + d.getCodigo(), null);
            }
            entity.setCodigo(d.getCodigo());
        }

        entity.setNombre(d.getNombre());
        entity.setContacto(d.getContacto());
        entity.setEmail(d.getEmail());
        entity.setTelefono(d.getTelefono());
        entity.setDireccion(d.getDireccion());
        entity.setCiudadPais(d.getCiudadPais());
        entity.setEstado(d.getEstado());

        Proveedor saved = proveedorRepository.saveAndFlush(entity);
        return ProveedorDto.build().fromEntity(saved);
    }

    @Override
    @Transactional
    public void delete(Integer d) {
        Proveedor entity = proveedorRepository.findById(Objects.requireNonNull(d, "id must not be null"))
                .orElseThrow(() -> new ValidationException("No se encontró el proveedor con ID: " + d));
        entity.setEstado(false);
        proveedorRepository.saveAndFlush(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProveedorDto> findAll() {
        ProveedorDto template = ProveedorDto.build();
        return proveedorRepository.findAll().stream()
                .map(proveedor -> ProveedorDto.build().fromEntity(template, proveedor))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProveedorDto findById(Integer d) {
        return proveedorRepository.findById(Objects.requireNonNull(d, "id must not be null"))
                .map(proveedor -> ProveedorDto.build().fromEntity(proveedor))
                .orElseThrow(() -> new ValidationException("Proveedor no encontrado con ID: " + d));
    }

    private static void validarCamposBaseProveedor(ProveedorDto dto) {
        ValidationUtil.isRequired(dto.getCodigo(), "El código del proveedor es obligatorio.");
        ValidationUtil.isRequired(dto.getNombre(), "El nombre del proveedor es obligatorio.");
    }

    private static void validateVersion(ProveedorDto dto, Proveedor entity) {
        if (dto.getVersion() != null && entity.getVersion() != null && !entity.getVersion().equals(dto.getVersion())) {
            ProveedorDto actual = ProveedorDto.build().fromEntity(new ProveedorDto(), entity);
            throw new ConflictException(
                    "El proveedor ha sido modificado por otro administrador. Por favor, recargue la página", actual);
        }
    }
}
