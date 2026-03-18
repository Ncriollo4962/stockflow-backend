package com.stockflow.core.service;

import com.stockflow.core.dto.ReporteInventarioFiltroRequest;
import com.stockflow.core.dto.ReporteKardexFiltroRequest;
import com.stockflow.core.dto.ReporteParetoVentasFiltroRequest;
import com.stockflow.core.dto.ReporteResponseDto;
import com.stockflow.core.dto.ReporteRotacionFiltroRequest;
import com.stockflow.core.dto.ReporteStockAlertasFiltroRequest;

public interface ReporteService {

    ReporteResponseDto inventarioValorizado(ReporteInventarioFiltroRequest filtro);

    ReporteResponseDto paretoAbcVentas(ReporteParetoVentasFiltroRequest filtro);

    ReporteResponseDto stockActualAlertas(ReporteStockAlertasFiltroRequest filtro);

    ReporteResponseDto kardexPorProducto(ReporteKardexFiltroRequest filtro);

    ReporteResponseDto analisisRotacion(ReporteRotacionFiltroRequest filtro);
}

