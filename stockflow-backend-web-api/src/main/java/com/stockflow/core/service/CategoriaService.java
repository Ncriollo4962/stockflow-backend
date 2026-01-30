package com.stockflow.core.service;

import com.stockflow.core.dto.CategoriaDto;

import java.util.List;

public interface CategoriaService {

    CategoriaDto save(CategoriaDto categoryDto);
    List<CategoriaDto> findAll();
    CategoriaDto findById(Integer id);
}
