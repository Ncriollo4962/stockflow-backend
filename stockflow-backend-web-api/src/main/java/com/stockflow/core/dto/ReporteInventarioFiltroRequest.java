package com.stockflow.core.dto;

public record ReporteInventarioFiltroRequest(
        Integer categoriaId,
        Boolean estado,
        Boolean soloConStock
) {}
