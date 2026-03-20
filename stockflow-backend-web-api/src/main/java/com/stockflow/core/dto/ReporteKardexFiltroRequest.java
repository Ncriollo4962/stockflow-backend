package com.stockflow.core.dto;

import java.time.LocalDateTime;

public record ReporteKardexFiltroRequest(
        Integer productoId,
        LocalDateTime desde,
        LocalDateTime hasta,
        String tipoMovimiento
) {}
