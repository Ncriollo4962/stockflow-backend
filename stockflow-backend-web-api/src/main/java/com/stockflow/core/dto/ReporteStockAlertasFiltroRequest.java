package com.stockflow.core.dto;

public record ReporteStockAlertasFiltroRequest(
        Integer categoriaId,
        Boolean estado,
        Boolean bajoMinimo
) {}
