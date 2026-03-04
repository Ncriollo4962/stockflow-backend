package com.stockflow.core.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.stockflow.core.dto.ProveedorDto;
import com.stockflow.core.repository.ProveedorRepository;
import com.stockflow.core.service.ProveedorService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorRepository proveedorRepository;

    @Override
    public ProveedorDto insert(ProveedorDto d) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'insert'");
    }

    @Override
    public ProveedorDto update(ProveedorDto d) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public void delete(ProveedorDto d) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    @Override
    public List<ProveedorDto> findAll() {
        ProveedorDto template = ProveedorDto.build();
        return proveedorRepository.findAll().stream()
                .map(proveedor -> ProveedorDto.build().fromEntity(template, proveedor))
                .toList();
    }

    @Override
    public ProveedorDto findById(Integer d) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findById'");
    }

}
