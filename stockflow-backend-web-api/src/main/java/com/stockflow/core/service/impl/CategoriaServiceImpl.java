package com.stockflow.core.service.impl;

import com.stockflow.core.dto.CategoriaDto;
import com.stockflow.core.entity.Categoria;
import com.stockflow.core.repository.CategoriaRepository;
import com.stockflow.core.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoryRepository;

    @Override
    @Transactional
    public CategoriaDto save(CategoriaDto categoryDto) {

        Categoria category = categoryDto.toEntity();

        Categoria savedCategory = categoryRepository.save(category);

        return CategoriaDto.build().fromEntity(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaDto> findAll() {
        return categoryRepository.findAll().stream()
                .map(category -> CategoriaDto.build().fromEntity(category))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaDto findById(Integer id) {
        return categoryRepository.findById(id)
                .map(category -> CategoriaDto.build().fromEntity(category))
                .orElse(null);
    }
}
