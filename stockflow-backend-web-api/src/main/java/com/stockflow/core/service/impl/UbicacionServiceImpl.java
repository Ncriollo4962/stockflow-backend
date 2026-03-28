package com.stockflow.core.service.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stockflow.core.dto.UbicacionDto;
import com.stockflow.core.entity.Ubicacion;
import com.stockflow.core.exception.ConflictException;
import com.stockflow.core.exception.ValidationException;
import com.stockflow.core.repository.UbicacionRepository;
import com.stockflow.core.service.UbicacionService;
import com.stockflow.core.utils.ValidationUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UbicacionServiceImpl implements UbicacionService {

    private final UbicacionRepository ubicacionRepository;

    @Override
    @Transactional
    public UbicacionDto insert(UbicacionDto dto) {
        ValidationUtil.isRequired(dto.getCodigo(), "El código es requerido.");
        ValidationUtil.isRequired(dto.getNombre(), "El nombre es requerido.");

        if (ubicacionRepository.findByCodigo(Objects.requireNonNull(dto.getCodigo(), "codigo must not be null")).isPresent()) {
            throw new ConflictException("Ya existe una ubicación con el código: " + dto.getCodigo(), null);
        }

        if (dto.getEstado() == null) {
            dto.setEstado(true);
        }

        Ubicacion entity = dto.toEntity();
        Ubicacion saved = ubicacionRepository.save(entity);
        return UbicacionDto.build().fromEntity(saved);
    }

    @Override
    @Transactional
    public UbicacionDto update(UbicacionDto dto) {
        Ubicacion oldEntity = ubicacionRepository.findById(Objects.requireNonNull(dto.getId(), "id must not be null"))
                .orElseThrow(() -> new ValidationException("Ubicación no encontrada"));

        if (dto.getCodigo() != null && !dto.getCodigo().equals(oldEntity.getCodigo())) {
            if (ubicacionRepository.findByCodigo(dto.getCodigo()).isPresent()) {
                throw new ConflictException("Ya existe una ubicación con el código: " + dto.getCodigo(), null);
            }
            oldEntity.setCodigo(dto.getCodigo());
        }

        if (dto.getNombre() != null) {
            oldEntity.setNombre(dto.getNombre());
        }
        if (dto.getDescripcion() != null) {
            oldEntity.setDescripcion(dto.getDescripcion());
        }
        if (dto.getEstado() != null) {
            oldEntity.setEstado(dto.getEstado());
        }

        Ubicacion saved = ubicacionRepository.save(oldEntity);
        return UbicacionDto.build().fromEntity(saved);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Ubicacion entity = ubicacionRepository.findById(Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new ValidationException("Ubicación no encontrada"));
        entity.setEstado(false);
        ubicacionRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UbicacionDto> findAll() {
        return ubicacionRepository.findAll().stream()
                .map(e -> UbicacionDto.build().fromEntity(e))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UbicacionDto findById(Integer id) {
        return ubicacionRepository.findById(Objects.requireNonNull(id, "id must not be null"))
                .map(e -> UbicacionDto.build().fromEntity(e))
                .orElseThrow(() -> new ValidationException("Ubicación no encontrada"));
    }
}
