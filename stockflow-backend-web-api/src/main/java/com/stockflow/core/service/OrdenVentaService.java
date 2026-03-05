package com.stockflow.core.service;

import com.stockflow.core.dto.OrdenVentaDto;
import com.stockflow.core.entity.OrdenVenta;
import com.stockflow.core.utils.common.GenericCrud;

public interface OrdenVentaService extends GenericCrud<OrdenVentaDto, OrdenVenta, Integer> {

    String generateNumeroOrden();
}
