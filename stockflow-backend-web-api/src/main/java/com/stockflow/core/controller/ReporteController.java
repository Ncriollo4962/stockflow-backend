package com.stockflow.core.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.stockflow.core.controller.contract.ReporteControllerOpenApi;
import com.stockflow.core.dto.ReporteInventarioFiltroRequest;
import com.stockflow.core.dto.ReporteKardexFiltroRequest;
import com.stockflow.core.dto.ReporteParetoVentasFiltroRequest;
import com.stockflow.core.dto.ReporteRotacionFiltroRequest;
import com.stockflow.core.dto.ReporteStockAlertasFiltroRequest;
import com.stockflow.core.service.ReporteService;
import com.stockflow.core.utils.common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ReporteController implements ReporteControllerOpenApi {

    private final ReporteService reporteService;

    @Override
    public ResponseEntity<ApiResponse> inventarioValorizado(ReporteInventarioFiltroRequest filtro) {
        return ResponseEntity.ok(ApiResponse.ok("Inventario valorizado", reporteService.inventarioValorizado(filtro)));
    }

    @Override
    public ResponseEntity<ApiResponse> paretoAbcVentas(ReporteParetoVentasFiltroRequest filtro) {
        return ResponseEntity.ok(ApiResponse.ok("Pareto ABC de ventas", reporteService.paretoAbcVentas(filtro)));
    }

    @Override
    public ResponseEntity<ApiResponse> stockActualAlertas(ReporteStockAlertasFiltroRequest filtro) {
        return ResponseEntity.ok(ApiResponse.ok("Stock actual con alertas", reporteService.stockActualAlertas(filtro)));
    }

    @Override
    public ResponseEntity<ApiResponse> kardexPorProducto(ReporteKardexFiltroRequest filtro) {
        return ResponseEntity.ok(ApiResponse.ok("Kardex por producto", reporteService.kardexPorProducto(filtro)));
    }

    @Override
    public ResponseEntity<ApiResponse> analisisRotacion(ReporteRotacionFiltroRequest filtro) {
        return ResponseEntity.ok(ApiResponse.ok("Análisis de rotación", reporteService.analisisRotacion(filtro)));
    }
}
