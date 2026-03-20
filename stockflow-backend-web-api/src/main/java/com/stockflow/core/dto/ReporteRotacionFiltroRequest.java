package com.stockflow.core.dto;

import java.time.LocalDateTime;

public record ReporteRotacionFiltroRequest(
        LocalDateTime desde,
        LocalDateTime hasta,
        Integer categoriaId,
        Boolean estado
) {}
