package com.stockflow.core.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stockflow.core.enums.EnumCodigoEstado;
import com.stockflow.core.repository.InventarioItemRepository;
import com.stockflow.core.repository.MovimientoInventarioRepository;
import com.stockflow.core.repository.OrdenCompraRepository;
import com.stockflow.core.repository.OrdenVentaRepository;
import com.stockflow.core.repository.ProductoRepository;
import com.stockflow.core.service.DashboardService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ProductoRepository productRepository;
    private final OrdenVentaRepository ordenVentaRepository;
    private final OrdenCompraRepository ordenCompraRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final InventarioItemRepository inventarioItemRepository;
    
    @Override
    @Transactional(readOnly = true)
    public Integer countProducts() {
        return Math.toIntExact(productRepository.count());
    }

    @Override
    @Transactional(readOnly = true)
    public Integer findProductsWithCriticalStock() {
        return Math.toIntExact(productRepository.countProductosConStockCritico());
    }

    @Override
    @Transactional(readOnly = true)
    public Integer countOrdenVentaByEstadoPendienteDespacho() {
        return Math.toIntExact(ordenVentaRepository.countByEstado(EnumCodigoEstado.PENDIENTE_DESPACHO.getCodigo()));
    }

    @Override
    @Transactional(readOnly = true)
    public Integer countOrdenCompraByEstadoPendienteRecepcion() {
        return Math.toIntExact(ordenCompraRepository.countByEstado(EnumCodigoEstado.PENDIENTE_RECEPCION.getCodigo()));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getMovimientosChartData(Integer year) {
        Integer yearConsulta = (year != null) ? year : LocalDate.now().getYear();

        // 2. Obtener datos crudos de la BD
        List<Map<String, Object>> rawData = movimientoInventarioRepository.getMovimientosPorMes(yearConsulta);

        // 2. Preparar estructuras para el gráfico
        String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
        List<Integer> entradas = new ArrayList<>();
        List<Integer> salidas = new ArrayList<>();

        // Inicializar listas con 0 para los 12 meses
        for (int i = 0; i < 12; i++) {
            entradas.add(0);
            salidas.add(0);
        }

        // 3. Mapear resultados de la BD a las listas (meses de 1 a 12 -> índices 0 a 11)
        for (Map<String, Object> row : rawData) {
            int mes = ((Number) row.get("mes")).intValue();
            int totalEntradas = row.get("totalEntradas") != null ? ((Number) row.get("totalEntradas")).intValue() : 0;
            int totalSalidas = row.get("totalSalidas") != null ? ((Number) row.get("totalSalidas")).intValue() : 0;

            if (mes >= 1 && mes <= 12) {
                entradas.set(mes - 1, totalEntradas);
                salidas.set(mes - 1, totalSalidas);
            }
        }

        // 4. Construir la respuesta con DATOS PUROS (Backend Agnostic)        
        Map<String, Object> response = new HashMap<>();
        response.put("labels", meses);
        
        List<Map<String, Object>> datasets = new ArrayList<>();
        
        Map<String, Object> datasetEntradas = new HashMap<>();
        datasetEntradas.put("label", "Entradas");
        datasetEntradas.put("data", entradas);
        
        Map<String, Object> datasetSalidas = new HashMap<>();
        datasetSalidas.put("label", "Salidas");
        datasetSalidas.put("data", salidas);
        
        datasets.add(datasetEntradas);
        datasets.add(datasetSalidas);
        
        response.put("datasets", datasets);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getComprasVentasChartData(Integer year) {
        Integer yearConsulta = (year != null) ? year : LocalDate.now().getYear();

        // 1. Obtener datos crudos de la BD
        List<Map<String, Object>> comprasRaw = ordenCompraRepository.getComprasPorMes(yearConsulta);
        List<Map<String, Object>> ventasRaw = ordenVentaRepository.getVentasPorMes(yearConsulta);

        // 2. Preparar estructuras para el gráfico
        String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
        List<Double> compras = new ArrayList<>();
        List<Double> ventas = new ArrayList<>();
        List<Double> saldoNeto = new ArrayList<>();

        // Inicializar listas con 0 para los 12 meses
        for (int i = 0; i < 12; i++) {
            compras.add(0.0);
            ventas.add(0.0);
            saldoNeto.add(0.0);
        }

        // 3. Mapear compras
        for (Map<String, Object> row : comprasRaw) {
            int mes = ((Number) row.get("mes")).intValue();
            double total = row.get("total") != null ? ((Number) row.get("total")).doubleValue() : 0.0;
            if (mes >= 1 && mes <= 12) {
                compras.set(mes - 1, total);
            }
        }

        // 4. Mapear ventas
        for (Map<String, Object> row : ventasRaw) {
            int mes = ((Number) row.get("mes")).intValue();
            double total = row.get("total") != null ? ((Number) row.get("total")).doubleValue() : 0.0;
            if (mes >= 1 && mes <= 12) {
                ventas.set(mes - 1, total);
            }
        }

        // 5. Calcular Saldo Neto (Ventas - Compras)
        for (int i = 0; i < 12; i++) {
            saldoNeto.set(i, ventas.get(i) - compras.get(i));
        }

        // 6. Construir la respuesta
        Map<String, Object> response = new HashMap<>();
        response.put("labels", meses);

        List<Map<String, Object>> datasets = new ArrayList<>();

        Map<String, Object> datasetCompras = new HashMap<>();
        datasetCompras.put("label", "Compras");
        datasetCompras.put("data", compras);
        datasets.add(datasetCompras);

        Map<String, Object> datasetVentas = new HashMap<>();
        datasetVentas.put("label", "Ventas");
        datasetVentas.put("data", ventas);
        datasets.add(datasetVentas);

        Map<String, Object> datasetSaldo = new HashMap<>();
        datasetSaldo.put("label", "Saldo Neto");
        datasetSaldo.put("data", saldoNeto);
        datasets.add(datasetSaldo);

        response.put("datasets", datasets);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getTopProductosSalidasChartData(Integer year) {
        Integer yearConsulta = (year != null) ? year : LocalDate.now().getYear();

        // 1. Obtener Top 10 productos con más salidas
        List<Map<String, Object>> topProductos = movimientoInventarioRepository.getTopProductosSalidas(yearConsulta);

        // 2. Preparar estructuras para el gráfico
        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();

        for (Map<String, Object> row : topProductos) {
            String producto = (String) row.get("producto");
            Long totalSalidas = row.get("totalSalidas") != null ? ((Number) row.get("totalSalidas")).longValue() : 0L;
            
            labels.add(producto);
            data.add(totalSalidas);
        }

        // 3. Construir la respuesta
        Map<String, Object> response = new HashMap<>();
        response.put("labels", labels);

        List<Map<String, Object>> datasets = new ArrayList<>();
        Map<String, Object> dataset = new HashMap<>();
        dataset.put("label", "Salidas");
        dataset.put("data", data);
        datasets.add(dataset);

        response.put("datasets", datasets);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getValorizacionInventarioChartData() {
        // 1. Obtener valorización por categoría
        List<Map<String, Object>> rawData = inventarioItemRepository.getValorizacionInventarioPorCategoria();

        // 2. Preparar estructuras para el gráfico
        List<String> labels = new ArrayList<>();
        List<Double> data = new ArrayList<>();

        for (Map<String, Object> row : rawData) {
            String categoria = (String) row.get("categoria");
            Double valorTotal = row.get("valorTotal") != null ? ((Number) row.get("valorTotal")).doubleValue() : 0.0;
            
            labels.add(categoria);
            data.add(valorTotal);
        }

        // 3. Construir la respuesta
        Map<String, Object> response = new HashMap<>();
        response.put("labels", labels);

        List<Map<String, Object>> datasets = new ArrayList<>();
        Map<String, Object> dataset = new HashMap<>();
        dataset.put("label", "Valorización (S/.)");
        dataset.put("data", data);
        datasets.add(dataset);

        response.put("datasets", datasets);
        return response;
    }
    
}
