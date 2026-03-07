package com.stockflow.core.service;

import java.util.List;

import com.stockflow.core.dto.MovimientoInventarioDto;
import com.stockflow.core.entity.MovimientoInventario;
import com.stockflow.core.utils.common.GenericCrud;

public interface MovimientoInventarioService extends GenericCrud<MovimientoInventarioDto, MovimientoInventario, Integer> {
    List<MovimientoInventarioDto> insertAll(List<MovimientoInventarioDto> dtos);
}
