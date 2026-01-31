package com.stockflow.core.service;

import com.stockflow.core.dto.ProductoDto;
import com.stockflow.core.entity.Producto;
import com.stockflow.core.utils.common.GenericCrud;

public interface ProductoService extends GenericCrud<ProductoDto, Producto, Integer> {
    ProductoDto findByCodigo(String codigo);

}
