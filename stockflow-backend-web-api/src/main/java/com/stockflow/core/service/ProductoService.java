package com.stockflow.core.service;

import com.stockflow.core.dto.ProductoDto;

import java.util.List;

public interface ProductoService {

    ProductoDto save(ProductoDto productDto);
    ProductoDto findById(Integer id);
    List<ProductoDto> findAll();
    ProductoDto findByCodigo(String codigo);

}
