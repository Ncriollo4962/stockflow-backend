package com.stockflow.core.service;

import com.stockflow.core.dto.InventarioItemDto;
import com.stockflow.core.entity.InventarioItem;
import com.stockflow.core.utils.common.GenericCrud;

public interface InventarioItemService extends GenericCrud<InventarioItemDto, InventarioItem, Integer> {
}
