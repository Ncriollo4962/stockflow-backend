package com.stockflow.core.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.stockflow.core.dto.ReporteInventarioFiltroRequest;
import com.stockflow.core.dto.ReporteKardexFiltroRequest;
import com.stockflow.core.dto.ReporteParetoVentasFiltroRequest;
import com.stockflow.core.dto.ReporteResponseDto;
import com.stockflow.core.dto.ReporteRotacionFiltroRequest;
import com.stockflow.core.dto.ReporteStockAlertasFiltroRequest;
import com.stockflow.core.entity.Categoria;
import com.stockflow.core.entity.Producto;
import com.stockflow.core.enums.EnumCodigoEstado;
import com.stockflow.core.handler.BusinessException;
import com.stockflow.core.repository.CategoriaRepository;
import com.stockflow.core.repository.DetalleOrdenVentaRepository;
import com.stockflow.core.repository.InventarioItemRepository;
import com.stockflow.core.repository.MovimientoInventarioRepository;
import com.stockflow.core.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class ReporteServiceImplTest {

    @Mock
    private InventarioItemRepository inventarioItemRepository;
    @Mock
    private CategoriaRepository categoriaRepository;
    @Mock
    private DetalleOrdenVentaRepository detalleOrdenVentaRepository;
    @Mock
    private MovimientoInventarioRepository movimientoInventarioRepository;
    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ReporteServiceImpl service;

    @Nested
    class InventarioValorizado {
        @Test
        void debeConstruirResumen_yClasificarEstadoAbastecimiento() {
            List<Map<String, Object>> raw = List.of(
                    rowInv("SKU1", "Agotado", "CAT", 0, 0, 0, 5, new BigDecimal("10"), BigDecimal.ZERO, new BigDecimal("-10"), new BigDecimal("0")),
                    rowInv("SKU2", "Critico", "CAT", 2, 0, 2, 5, new BigDecimal("10"), new BigDecimal("20"), new BigDecimal("20"), new BigDecimal("20")),
                    rowInv("SKU3", "Bajo", "CAT", 7, 0, 7, 5, new BigDecimal("10"), new BigDecimal("20"), new BigDecimal("70"), new BigDecimal("70")),
                    rowInv("SKU4", "Sobre", "CAT", 20, 0, 20, 5, new BigDecimal("10"), new BigDecimal("20"), new BigDecimal("200"), new BigDecimal("200")),
                    rowInv("SKU5", "Optimo", "CAT", 12, 0, 12, 5, new BigDecimal("10"), new BigDecimal("20"), new BigDecimal("120"), new BigDecimal("120")));

            when(inventarioItemRepository.getInventarioValorizadoPorProducto(eq(null), eq(null), eq(false))).thenReturn(raw);

            ReporteResponseDto response = service.inventarioValorizado(null);

            assertNotNull(response);
            assertEquals("Inventario Valorizado - Todas", response.reporte());
            assertNotNull(response.resumen());
            assertEquals(5, response.resumen().get("totalItems"));
            assertEquals(new BigDecimal("410"), response.resumen().get("valorTotal"));
            assertEquals(3, response.resumen().get("productosEnRiesgo"));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) response.data();
            assertEquals(5, data.size());
            assertEquals("Agotado", data.get(0).get("estadoAbastecimiento"));
            assertEquals("Critico", data.get(1).get("estadoAbastecimiento"));
            assertEquals("Stock Bajo", data.get(2).get("estadoAbastecimiento"));
            assertEquals("Sobrestock", data.get(3).get("estadoAbastecimiento"));
            assertEquals("Optimo", data.get(4).get("estadoAbastecimiento"));
            assertNotNull(data.get(2).get("participacionPorcentaje"));
        }

        @Test
        void debeResolverCategoriaPorId_cuandoFiltroIncluyeCategoriaId() {
            when(categoriaRepository.findById(eq(1))).thenReturn(Optional.of(Categoria.builder().id(1).nombre("Ferreteria").build()));
            when(inventarioItemRepository.getInventarioValorizadoPorProducto(eq(1), eq(true), eq(true))).thenReturn(List.of());

            ReporteResponseDto response = service.inventarioValorizado(new ReporteInventarioFiltroRequest(1, true, true));

            assertNotNull(response);
            assertEquals("Inventario Valorizado - Ferreteria", response.reporte());
            assertEquals("Ferreteria", response.resumen().get("categoria"));
            assertEquals(true, response.resumen().get("soloConStock"));
        }
    }

    @Nested
    class ParetoAbcVentas {
        @Test
        void debeLanzarBusinessException_cuandoFechasSonNulas() {
            BusinessException ex = assertThrows(BusinessException.class, () -> service.paretoAbcVentas(null));
            assertEquals("Los parámetros 'desde' y 'hasta' son requeridos.", ex.getMessage());
        }

        @Test
        void debeClasificarABC_cuandoExisteData() {
            LocalDateTime desde = LocalDateTime.of(2025, 1, 1, 0, 0);
            LocalDateTime hasta = LocalDateTime.of(2025, 1, 31, 23, 59);
            ReporteParetoVentasFiltroRequest filtro = new ReporteParetoVentasFiltroRequest(desde, hasta, null, null);

            List<Map<String, Object>> raw = List.of(
                    rowPareto("SKU1", "P1", "CAT", 10, new BigDecimal("10"), new BigDecimal("80")),
                    rowPareto("SKU2", "P2", "CAT", 2, new BigDecimal("10"), new BigDecimal("10")),
                    rowPareto("SKU3", "P3", "CAT", 1, new BigDecimal("10"), new BigDecimal("10")));

            when(detalleOrdenVentaRepository.getVentasPorProductoAbc(any(), any(), eq(EnumCodigoEstado.ANULADA.getCodigo()), eq(null), eq(null)))
                    .thenReturn(raw);

            ReporteResponseDto response = service.paretoAbcVentas(filtro);

            assertNotNull(response);
            assertTrue(response.reporte().startsWith("Análisis Pareto ABC - Ventas"));
            assertEquals(new BigDecimal("100"), response.resumen().get("valorTotal"));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) response.data();
            assertEquals("A", data.get(0).get("clase"));
            assertEquals("B", data.get(1).get("clase"));
            assertEquals("C", data.get(2).get("clase"));
        }
    }

    @Nested
    class StockActualAlertas {
        @Test
        void debeCalcularAlertas_yResumen() {
            List<Map<String, Object>> raw = List.of(
                    rowStockAlertas(1, "SKU1", "P1", 0, 0, 0, 5),
                    rowStockAlertas(2, "SKU2", "P2", 11, 0, 11, 5));

            when(inventarioItemRepository.getStockAlertas(eq(null), eq(null), eq(false))).thenReturn(raw);

            ReporteResponseDto response = service.stockActualAlertas(new ReporteStockAlertasFiltroRequest(null, null, false));

            assertNotNull(response);
            assertEquals("Stock Actual y Alertas - Todas", response.reporte());
            assertEquals(2, response.resumen().get("totalItems"));
            assertEquals(1, response.resumen().get("productosEnRiesgo"));
            assertEquals(1, response.resumen().get("productosSinStock"));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) response.data();
            assertEquals(true, data.get(0).get("alerta"));
            assertEquals(false, data.get(1).get("alerta"));
        }
    }

    @Nested
    class KardexPorProducto {
        @Test
        void debeCalcularSaldosEntradasSalidas_yResumen() {
            LocalDateTime desde = LocalDateTime.of(2025, 1, 1, 0, 0);
            LocalDateTime hasta = LocalDateTime.of(2025, 1, 31, 0, 0);
            ReporteKardexFiltroRequest filtro = new ReporteKardexFiltroRequest(10, desde, hasta, null);

            when(productoRepository.findById(eq(10))).thenReturn(Optional.of(Producto.builder().id(10).codigo("P1").nombre("Taladro").build()));
            when(movimientoInventarioRepository.getSaldoAntesDe(eq(10), any())).thenReturn(10);

            List<Map<String, Object>> raw = List.of(
                    rowKardex("ENTRADA", 5),
                    rowKardex("SALIDA", 3),
                    rowKardex("OTRO", 99));

            when(movimientoInventarioRepository.getKardexPorProducto(eq(10), eq(null), any(), any())).thenReturn(raw);

            ReporteResponseDto response = service.kardexPorProducto(filtro);

            assertNotNull(response);
            assertTrue(response.reporte().startsWith("Kardex por Producto - P1 - Taladro"));
            assertEquals(10, response.resumen().get("saldoInicial"));
            assertEquals(5, response.resumen().get("totalEntradas"));
            assertEquals(3, response.resumen().get("totalSalidas"));
            assertEquals(12, response.resumen().get("saldoFinal"));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) response.data();
            assertEquals(15, data.get(0).get("saldoAcumulado"));
            assertEquals(12, data.get(1).get("saldoAcumulado"));
            assertEquals(12, data.get(2).get("saldoAcumulado"));
        }

        @Test
        void debeLanzarBusinessException_cuandoFechasSonNulas() {
            ReporteKardexFiltroRequest filtro = new ReporteKardexFiltroRequest(10, null, null, null);
            BusinessException ex = assertThrows(BusinessException.class, () -> service.kardexPorProducto(filtro));
            assertEquals("Los parámetros 'desde' y 'hasta' son requeridos.", ex.getMessage());
        }
    }

    @Nested
    class AnalisisRotacion {
        @Test
        void debeClasificarProductos_yContarHuesos() {
            LocalDateTime desde = LocalDateTime.of(2025, 1, 1, 0, 0);
            LocalDateTime hasta = LocalDateTime.of(2025, 1, 31, 0, 0);
            ReporteRotacionFiltroRequest filtro = new ReporteRotacionFiltroRequest(desde, hasta, null, null);

            List<Map<String, Object>> ventasRaw = List.of(
                    rowVentasCantidad(1, 0),
                    rowVentasCantidad(2, 25),
                    rowVentasCantidad(3, 6),
                    rowVentasCantidad(4, 1));
            when(detalleOrdenVentaRepository.getCantidadVentasPorProducto(any(), any(), eq(EnumCodigoEstado.ANULADA.getCodigo()), eq(null), eq(null)))
                    .thenReturn(ventasRaw);

            List<Map<String, Object>> invRaw = List.of(
                    rowStockActual(1, 10, 0),
                    rowStockActual(2, 10, 2),
                    rowStockActual(3, 10, 0),
                    rowStockActual(4, 10, 20));
            when(inventarioItemRepository.getStockActualPorProducto(eq(null), eq(null))).thenReturn(invRaw);

            Categoria cat = Categoria.builder().id(1).nombre("CAT").build();
            List<Producto> productos = List.of(
                    Producto.builder().id(1).codigo("P1").nombre("Prod 1").categoria(cat).build(),
                    Producto.builder().id(2).codigo("P2").nombre("Prod 2").categoria(cat).build(),
                    Producto.builder().id(3).codigo("P3").nombre("Prod 3").categoria(cat).build(),
                    Producto.builder().id(4).codigo("P4").nombre("Prod 4").categoria(cat).build());
            when(productoRepository.findForReporte(eq(null), eq(null))).thenReturn(productos);

            ReporteResponseDto response = service.analisisRotacion(filtro);

            assertNotNull(response);
            assertTrue(response.reporte().startsWith("Analisis de Rotacion - Todas"));
            assertEquals(1, response.resumen().get("productosHueso"));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) response.data();
            assertEquals(4, data.size());
            assertEquals("ALTA", data.get(0).get("clasificacion"));
            assertEquals("MEDIA", data.get(1).get("clasificacion"));
            assertEquals("BAJA", data.get(2).get("clasificacion"));
            assertEquals("HUESO", data.get(3).get("clasificacion"));
            assertEquals(0, data.get(3).get("cantidadVendidaPeriodo"));
        }
    }

    private static Map<String, Object> rowInv(
            String sku,
            String nombre,
            String categoria,
            Integer stock,
            Integer reservado,
            Integer stockDisponible,
            Integer cantidadMinima,
            BigDecimal costoUnitario,
            BigDecimal precioVenta,
            BigDecimal valorTotal,
            BigDecimal valorDisponible) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("sku", sku);
        row.put("nombre", nombre);
        row.put("categoria", categoria);
        row.put("stock", stock);
        row.put("reservado", reservado);
        row.put("stockDisponible", stockDisponible);
        row.put("cantidadMinima", cantidadMinima);
        row.put("costoUnitario", costoUnitario);
        row.put("precioVenta", precioVenta);
        row.put("valorTotal", valorTotal);
        row.put("valorDisponible", valorDisponible);
        row.put("fechaUltimoMovimiento", LocalDateTime.of(2025, 1, 1, 0, 0));
        return row;
    }

    private static Map<String, Object> rowPareto(
            String sku,
            String nombre,
            String categoria,
            Integer cantidadVendida,
            BigDecimal precioUnitario,
            BigDecimal valorVentas) {
        Map<String, Object> row = new HashMap<>();
        row.put("sku", sku);
        row.put("nombre", nombre);
        row.put("categoria", categoria);
        row.put("cantidadVendida", cantidadVendida);
        row.put("precioUnitario", precioUnitario);
        row.put("valorVentas", valorVentas);
        return row;
    }

    private static Map<String, Object> rowStockAlertas(
            Integer id,
            String sku,
            String nombre,
            Integer stock,
            Integer reservado,
            Integer stockDisponible,
            Integer cantidadMinima) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", id);
        row.put("sku", sku);
        row.put("nombre", nombre);
        row.put("categoriaId", 1);
        row.put("categoria", "CAT");
        row.put("stock", stock);
        row.put("reservado", reservado);
        row.put("stockDisponible", stockDisponible);
        row.put("cantidadMinima", cantidadMinima);
        row.put("fechaUltimoMovimiento", LocalDateTime.of(2025, 1, 1, 0, 0));
        return row;
    }

    private static Map<String, Object> rowKardex(String tipoMovimiento, Integer cantidad) {
        Map<String, Object> row = new HashMap<>();
        row.put("fechaMovimiento", LocalDateTime.of(2025, 1, 1, 0, 0));
        row.put("tipoMovimiento", tipoMovimiento);
        row.put("cantidad", cantidad);
        row.put("ubicacion", "U");
        row.put("usuario", "user");
        row.put("motivo", "m");
        row.put("referencia", "r");
        row.put("notas", "n");
        return row;
    }

    private static Map<String, Object> rowVentasCantidad(Integer id, Integer cantidadVendida) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", id);
        row.put("cantidadVendida", cantidadVendida);
        return row;
    }

    private static Map<String, Object> rowStockActual(Integer id, Integer stock, Integer reservado) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", id);
        row.put("stock", stock);
        row.put("reservado", reservado);
        row.put("stockDisponible", stock != null && reservado != null ? stock - reservado : null);
        return row;
    }
}
