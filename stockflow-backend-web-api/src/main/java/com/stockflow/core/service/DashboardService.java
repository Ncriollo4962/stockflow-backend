package com.stockflow.core.service;

import java.util.Map;

public interface DashboardService {

    Integer countProducts();
    Integer findProductsWithCriticalStock();
    Integer countOrdenVentaByEstadoPendienteDespacho();
    Integer countOrdenCompraByEstadoPendienteRecepcion();
    Map<String, Object> getMovimientosChartData(Integer year);
    Map<String, Object> getComprasVentasChartData(Integer year);
    Map<String, Object> getTopProductosSalidasChartData(Integer year);
    Map<String, Object> getValorizacionInventarioChartData();
}
