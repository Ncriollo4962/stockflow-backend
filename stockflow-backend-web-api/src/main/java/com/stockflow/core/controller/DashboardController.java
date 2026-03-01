package com.stockflow.core.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.stockflow.core.controller.contract.DashboardControllerOpenApi;
import com.stockflow.core.service.DashboardService;
import com.stockflow.core.utils.common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DashboardController implements DashboardControllerOpenApi {
    
    private final DashboardService dashboardService;

    @Override
    public ResponseEntity<ApiResponse> countProducts() {
        Integer countProducts = dashboardService.countProducts();
        return ResponseEntity.ok(ApiResponse.ok("Conteo de productos", countProducts));
    }

    @Override
    public ResponseEntity<ApiResponse> countProductsWithCriticalStock() {
        Integer countProductsWithCriticalStock = dashboardService.findProductsWithCriticalStock();
        return ResponseEntity.ok(ApiResponse.ok("Conteo de productos con stock crítico", countProductsWithCriticalStock));
    }
    
    @Override
    public ResponseEntity<ApiResponse> countOrdenVentaByEstadoPendienteDespacho() {
        Integer countOrdenVentaByEstadoPendienteDespacho = dashboardService.countOrdenVentaByEstadoPendienteDespacho();
        return ResponseEntity.ok(ApiResponse.ok("Conteo de órdenes de venta pendientes de despacho", countOrdenVentaByEstadoPendienteDespacho));
    }

    @Override
    public ResponseEntity<ApiResponse> countOrdenCompraByEstadoPendienteRecepcion() {
        Integer countOrdenCompraByEstadoPendienteRecepcion = dashboardService.countOrdenCompraByEstadoPendienteRecepcion();
        return ResponseEntity.ok(ApiResponse.ok("Conteo de órdenes de compra pendientes de recepción", countOrdenCompraByEstadoPendienteRecepcion));
    }

    @Override
    public ResponseEntity<ApiResponse> getMovimientosChartData(Integer year) {
        Map<String, Object> data = dashboardService.getMovimientosChartData(year);
        return ResponseEntity.ok(ApiResponse.ok("Datos del gráfico de movimientos", data));
    }

    @Override
    public ResponseEntity<ApiResponse> getComprasVentasChartData(Integer year) {
        Map<String, Object> data = dashboardService.getComprasVentasChartData(year);
        return ResponseEntity.ok(ApiResponse.ok("Datos del gráfico de compras vs ventas", data));
    }

    @Override
    public ResponseEntity<ApiResponse> getTopProductosSalidasChartData(Integer year) {
        Map<String, Object> data = dashboardService.getTopProductosSalidasChartData(year);
        return ResponseEntity.ok(ApiResponse.ok("Datos del gráfico de Top 10 productos con mayor rotación", data));
    }

    @Override
    public ResponseEntity<ApiResponse> getValorizacionInventarioChartData() {
        Map<String, Object> data = dashboardService.getValorizacionInventarioChartData();
        return ResponseEntity.ok(ApiResponse.ok("Datos del gráfico de valorización del inventario", data));
    }

}
