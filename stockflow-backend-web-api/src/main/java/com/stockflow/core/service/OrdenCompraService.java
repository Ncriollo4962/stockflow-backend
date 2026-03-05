package com.stockflow.core.service;

import com.stockflow.core.dto.OrdenCompraDto;
import com.stockflow.core.entity.OrdenCompra;
import com.stockflow.core.utils.common.GenericCrud;

public interface OrdenCompraService extends GenericCrud<OrdenCompraDto, OrdenCompra, Integer> {

    String generateNumeroOrden();
}
