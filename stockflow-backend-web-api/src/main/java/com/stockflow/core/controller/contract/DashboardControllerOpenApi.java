package com.stockflow.core.controller.contract;


import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.stockflow.core.utils.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RequestMapping("/api/dashboard")
public interface DashboardControllerOpenApi {

    @Operation(summary = "Obtiene el conteo de productos")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Integer.class)))
    @GetMapping(path = "/count-products", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> countProducts();

    @Operation(summary = "Obtiene el conteo de productos con stock crítico")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Integer.class)))
    @GetMapping(path = "/count-products-critical-stock", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> countProductsWithCriticalStock();

    @Operation(summary = "Obtiene el conteo de órdenes de venta pendientes de despacho")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Integer.class)))
    @GetMapping(path = "/count-orden-venta-pendiente-despacho", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> countOrdenVentaByEstadoPendienteDespacho();

    @Operation(summary = "Obtiene el conteo de órdenes de compra pendientes de recepción")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Integer.class)))
    @GetMapping(path = "/count-orden-compra-pendiente-recepcion", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> countOrdenCompraByEstadoPendienteRecepcion();

    @Operation(summary = "Obtiene los datos para el gráfico de movimientos de inventario")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Object.class)))
    @GetMapping(path = "/chart-movimientos/{year}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> getMovimientosChartData(@PathVariable Integer year);

    @Operation(summary = "Obtiene los datos para el gráfico de compras vs ventas")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Object.class)))
    @GetMapping(path = "/chart-compras-ventas/{year}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> getComprasVentasChartData(@PathVariable Integer year);

    @Operation(summary = "Obtiene los datos para el gráfico de Top 10 productos con mayor rotación (salidas)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Object.class)))
    @GetMapping(path = "/chart-top-productos-salidas/{year}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> getTopProductosSalidasChartData(@PathVariable Integer year);

    @Operation(summary = "Obtiene los datos para el gráfico de Valorización del Inventario por Categoría")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Object.class)))
    @GetMapping(path = "/chart-valorizacion-inventario-categoria", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> getValorizacionInventarioChartData();
    
}
