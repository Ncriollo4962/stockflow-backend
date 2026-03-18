package com.stockflow.core.controller.contract;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.stockflow.core.dto.ReporteInventarioFiltroRequest;
import com.stockflow.core.dto.ReporteKardexFiltroRequest;
import com.stockflow.core.dto.ReporteParetoVentasFiltroRequest;
import com.stockflow.core.dto.ReporteResponseDto;
import com.stockflow.core.dto.ReporteRotacionFiltroRequest;
import com.stockflow.core.dto.ReporteStockAlertasFiltroRequest;
import com.stockflow.core.utils.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springdoc.core.annotations.ParameterObject;

@RequestMapping("/api/reportes")
public interface ReporteControllerOpenApi {

    @Operation(summary = "Reporte de inventario valorizado (por sucursal opcional)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ReporteResponseDto.class))
    )
    @GetMapping(path = "/inventario-valorizado", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> inventarioValorizado(@ParameterObject ReporteInventarioFiltroRequest filtro);

    @Operation(summary = "Reporte de análisis Pareto ABC de ventas (por rango de fechas)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ReporteResponseDto.class))
    )
    @GetMapping(path = "/pareto-abc-ventas", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> paretoAbcVentas(@ParameterObject ReporteParetoVentasFiltroRequest filtro);

    @Operation(summary = "Reporte de stock actual y alertas (quiebre/stock mínimo)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ReporteResponseDto.class))
    )
    @GetMapping(path = "/stock-alertas", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> stockActualAlertas(@ParameterObject ReporteStockAlertasFiltroRequest filtro);

    @Operation(summary = "Reporte kardex por producto (historial de movimientos)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ReporteResponseDto.class))
    )
    @GetMapping(path = "/kardex", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> kardexPorProducto(
            @ParameterObject ReporteKardexFiltroRequest filtro
    );

    @Operation(summary = "Reporte de análisis de rotación (productos hueso vs alta rotación)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ReporteResponseDto.class))
    )
    @GetMapping(path = "/rotacion", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> analisisRotacion(@ParameterObject ReporteRotacionFiltroRequest filtro);
}

