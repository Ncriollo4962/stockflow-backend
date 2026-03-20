package com.stockflow.core.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.stockflow.core.enums.EnumCodigoEstado;
import com.stockflow.core.repository.InventarioItemRepository;
import com.stockflow.core.repository.MovimientoInventarioRepository;
import com.stockflow.core.repository.OrdenCompraRepository;
import com.stockflow.core.repository.OrdenVentaRepository;
import com.stockflow.core.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private ProductoRepository productRepository;
    @Mock
    private OrdenVentaRepository ordenVentaRepository;
    @Mock
    private OrdenCompraRepository ordenCompraRepository;
    @Mock
    private MovimientoInventarioRepository movimientoInventarioRepository;
    @Mock
    private InventarioItemRepository inventarioItemRepository;

    @InjectMocks
    private DashboardServiceImpl service;

    @Nested
    class Counts {
        @Test
        void countProducts_debeConvertirLongAInt() {
            when(productRepository.count()).thenReturn(5L);

            Integer result = service.countProducts();

            assertEquals(5, result);
        }

        @Test
        void findProductsWithCriticalStock_debeRetornarConteo() {
            when(productRepository.countProductosConStockCritico()).thenReturn(2L);

            Integer result = service.findProductsWithCriticalStock();

            assertEquals(2, result);
        }

        @Test
        void countOrdenVentaByEstadoPendienteDespacho_debeUsarEstadoEnum() {
            when(ordenVentaRepository.countByEstado(eq(EnumCodigoEstado.PENDIENTE_DESPACHO.getCodigo()))).thenReturn(3L);

            Integer result = service.countOrdenVentaByEstadoPendienteDespacho();

            assertEquals(3, result);
        }

        @Test
        void countOrdenCompraByEstadoPendienteRecepcion_debeUsarEstadoEnum() {
            when(ordenCompraRepository.countByEstado(eq(EnumCodigoEstado.PENDIENTE_RECEPCION.getCodigo()))).thenReturn(4L);

            Integer result = service.countOrdenCompraByEstadoPendienteRecepcion();

            assertEquals(4, result);
        }
    }

    @Nested
    class Charts {
        @Test
        void getMovimientosChartData_debeInicializar12Meses_yMapearData() {
            int year = 2025;
            Map<String, Object> rowEne = new HashMap<>();
            rowEne.put("mes", 1);
            rowEne.put("totalEntradas", 5);
            rowEne.put("totalSalidas", 2);

            Map<String, Object> rowMar = new HashMap<>();
            rowMar.put("mes", 3);
            rowMar.put("totalEntradas", null);
            rowMar.put("totalSalidas", 7);

            when(movimientoInventarioRepository.getMovimientosPorMes(eq(year))).thenReturn(List.of(rowEne, rowMar));

            Map<String, Object> response = service.getMovimientosChartData(year);

            assertEquals(12, ((String[]) response.get("labels")).length);
            List<?> datasets = (List<?>) response.get("datasets");
            assertEquals(2, datasets.size());

            Map<?, ?> entradas = (Map<?, ?>) datasets.get(0);
            Map<?, ?> salidas = (Map<?, ?>) datasets.get(1);

            @SuppressWarnings("unchecked")
            List<Integer> dataEntradas = (List<Integer>) entradas.get("data");
            @SuppressWarnings("unchecked")
            List<Integer> dataSalidas = (List<Integer>) salidas.get("data");

            assertEquals(12, dataEntradas.size());
            assertEquals(12, dataSalidas.size());
            assertEquals(5, dataEntradas.get(0));
            assertEquals(2, dataSalidas.get(0));
            assertEquals(0, dataEntradas.get(2));
            assertEquals(7, dataSalidas.get(2));
        }

        @Test
        void getMovimientosChartData_debeUsarAnioActual_cuandoYearEsNulo() {
            int currentYear = LocalDate.now().getYear();
            when(movimientoInventarioRepository.getMovimientosPorMes(eq(currentYear))).thenReturn(List.of());

            Map<String, Object> response = service.getMovimientosChartData(null);

            assertNotNull(response);
            verify(movimientoInventarioRepository).getMovimientosPorMes(eq(currentYear));
        }

        @Test
        void getComprasVentasChartData_debeCalcularSaldoNeto() {
            int year = 2025;
            Map<String, Object> compraEne = new HashMap<>();
            compraEne.put("mes", 1);
            compraEne.put("total", 100.0);

            Map<String, Object> ventaEne = new HashMap<>();
            ventaEne.put("mes", 1);
            ventaEne.put("total", 160.0);

            when(ordenCompraRepository.getComprasPorMes(eq(year))).thenReturn(List.of(compraEne));
            when(ordenVentaRepository.getVentasPorMes(eq(year))).thenReturn(List.of(ventaEne));

            Map<String, Object> response = service.getComprasVentasChartData(year);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> datasets = (List<Map<String, Object>>) response.get("datasets");
            assertEquals(3, datasets.size());

            @SuppressWarnings("unchecked")
            List<Double> compras = (List<Double>) datasets.get(0).get("data");
            @SuppressWarnings("unchecked")
            List<Double> ventas = (List<Double>) datasets.get(1).get("data");
            @SuppressWarnings("unchecked")
            List<Double> saldo = (List<Double>) datasets.get(2).get("data");

            assertEquals(100.0, compras.get(0));
            assertEquals(160.0, ventas.get(0));
            assertEquals(60.0, saldo.get(0));
        }

        @Test
        void getTopProductosSalidasChartData_debeMapearLabelsYData() {
            int year = 2025;
            Map<String, Object> row = new HashMap<>();
            row.put("producto", "P1");
            row.put("totalSalidas", 9);

            when(movimientoInventarioRepository.getTopProductosSalidas(eq(year))).thenReturn(List.of(row));

            Map<String, Object> response = service.getTopProductosSalidasChartData(year);

            @SuppressWarnings("unchecked")
            List<String> labels = (List<String>) response.get("labels");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> datasets = (List<Map<String, Object>>) response.get("datasets");
            @SuppressWarnings("unchecked")
            List<Long> data = (List<Long>) datasets.get(0).get("data");

            assertEquals(List.of("P1"), labels);
            assertEquals(List.of(9L), data);
        }

        @Test
        void getValorizacionInventarioChartData_debeMapearResultados() {
            Map<String, Object> row = new HashMap<>();
            row.put("categoria", "CAT");
            row.put("valorTotal", 250.0);

            when(inventarioItemRepository.getValorizacionInventarioPorCategoria()).thenReturn(List.of(row));

            Map<String, Object> response = service.getValorizacionInventarioChartData();

            @SuppressWarnings("unchecked")
            List<String> labels = (List<String>) response.get("labels");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> datasets = (List<Map<String, Object>>) response.get("datasets");

            assertEquals(List.of("CAT"), labels);
            assertTrue(((List<?>) datasets.get(0).get("data")).size() == 1);
        }
    }
}

