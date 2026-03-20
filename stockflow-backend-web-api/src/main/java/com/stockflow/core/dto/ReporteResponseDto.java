package com.stockflow.core.dto;

import java.util.List;
import java.util.Map;

public record ReporteResponseDto(
        String reporte,
        String fechaGeneracion,
        Map<String, Object> resumen,
        List<Map<String, Object>> data
) {}
