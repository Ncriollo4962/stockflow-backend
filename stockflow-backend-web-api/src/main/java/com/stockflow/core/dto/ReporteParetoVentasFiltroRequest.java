package com.stockflow.core.dto;

import java.time.LocalDateTime;

public record ReporteParetoVentasFiltroRequest(
        LocalDateTime desde,
        LocalDateTime hasta,
        Integer categoriaId,
        Boolean estado
) {}
