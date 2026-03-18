package com.stockflow.core.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.stockflow.core.service.ReporteService;
import com.stockflow.core.utils.common.TypeException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReporteServiceImpl implements ReporteService {

    private static final String MONEDA_DEFAULT = "PEN";
    private static final BigDecimal UMBRAL_A = new BigDecimal("0.80");
    private static final BigDecimal UMBRAL_B = new BigDecimal("0.95");
    private static final DateTimeFormatter TITULO_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final InventarioItemRepository inventarioItemRepository;
    private final CategoriaRepository categoriaRepository;
    private final DetalleOrdenVentaRepository detalleOrdenVentaRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final ProductoRepository productoRepository;

    @Override
    @Transactional(readOnly = true)
    public ReporteResponseDto inventarioValorizado(ReporteInventarioFiltroRequest filtro) {

        Integer categoriaId = filtro != null ? filtro.categoriaId() : null;
        Boolean estado = filtro != null ? filtro.estado() : null;
        Boolean soloConStock = filtro != null && Boolean.TRUE.equals(filtro.soloConStock());

        String categoria = resolveCategoriaNombre(categoriaId);

        List<Map<String, Object>> raw = inventarioItemRepository
                .getInventarioValorizadoPorProducto(categoriaId, estado, soloConStock);

        BigDecimal valorTotalGeneral = BigDecimal.ZERO;
        int productosEnRiesgo = 0;
        List<Map<String, Object>> data = new ArrayList<>();

        for (Map<String, Object> row : raw) {

            String sku = Objects.toString(row.get("sku"), null);
            String nombre = Objects.toString(row.get("nombre"), null);
            String categoriaItem = Objects.toString(row.get("categoria"), null);
            Integer stock = Objects.requireNonNullElse(toInteger(row.get("stock")), 0);
            Integer reservado = Objects.requireNonNullElse(toInteger(row.get("reservado")), 0);
            Integer stockDisponible = Objects.requireNonNullElse(toInteger(row.get("stockDisponible")), 0);
            Integer cantidadMinima = Objects.requireNonNullElse(toInteger(row.get("cantidadMinima")), 0);
            BigDecimal costoUnitario = toBigDecimal(row.get("costoUnitario"));
            BigDecimal precioVenta = toBigDecimal(row.get("precioVenta"));
            BigDecimal valorItem = toBigDecimal(row.get("valorTotal")).max(BigDecimal.ZERO);
            BigDecimal valorDisponible = toBigDecimal(row.get("valorDisponible")).max(BigDecimal.ZERO);
            LocalDateTime fechaUltimoMov = toLocalDateTime(row.get("fechaUltimoMovimiento"));

            valorTotalGeneral = valorTotalGeneral.add(valorItem);

            String estadoAbastecimiento;
            if (stock == 0) {
                estadoAbastecimiento = "Agotado";
                productosEnRiesgo++;
            } else if (cantidadMinima > 0 && stock <= cantidadMinima) {
                estadoAbastecimiento = "Critico";
                productosEnRiesgo++;
            } else if (cantidadMinima > 0 && stock <= cantidadMinima * 2) {
                estadoAbastecimiento = "Stock Bajo";
                productosEnRiesgo++;
            } else if (cantidadMinima > 0 && stock > cantidadMinima * 3) {
                estadoAbastecimiento = "Sobrestock";
            } else {
                estadoAbastecimiento = "Optimo";
            }

            BigDecimal margen = precioVenta.signum() == 0
                    ? BigDecimal.ZERO
                    : precioVenta.subtract(costoUnitario)
                            .divide(precioVenta, 8, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"))
                            .setScale(2, RoundingMode.HALF_UP);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("sku", sku);
            item.put("nombre", nombre);
            item.put("categoria", categoriaItem);
            item.put("stock", stock);
            item.put("reservado", reservado);
            item.put("stockDisponible", stockDisponible);
            item.put("costoUnitario", costoUnitario);
            item.put("precioVenta", precioVenta);
            item.put("cantidadMinima", cantidadMinima);
            item.put("valorTotal", valorItem);
            item.put("valorDisponible", valorDisponible);
            item.put("participacionPorcentaje", BigDecimal.ZERO);
            item.put("estadoAbastecimiento", estadoAbastecimiento);
            item.put("fechaUltimoMovimiento", fechaUltimoMov);
            item.put("margenContribucionPorcentaje", margen);
            data.add(item);
        }

        for (Map<String, Object> item : data) {
            BigDecimal valorItem = toBigDecimal(item.get("valorTotal"));
            BigDecimal participacion = valorTotalGeneral.signum() == 0
                    ? BigDecimal.ZERO
                    : valorItem.divide(valorTotalGeneral, 8, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"))
                            .setScale(2, RoundingMode.HALF_UP);
            item.put("participacionPorcentaje", participacion);
        }

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("categoria", categoria);
        resumen.put("estado", estado);
        resumen.put("soloConStock", soloConStock);
        resumen.put("totalItems", data.size());
        resumen.put("valorTotal", valorTotalGeneral);
        resumen.put("moneda", MONEDA_DEFAULT);
        resumen.put("productosEnRiesgo", productosEnRiesgo);

        return buildReport("Inventario Valorizado - " + categoria, resumen, data);
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteResponseDto paretoAbcVentas(ReporteParetoVentasFiltroRequest filtro) {
        LocalDateTime desde = filtro != null ? filtro.desde() : null;
        LocalDateTime hasta = filtro != null ? filtro.hasta() : null;
        Integer categoriaId = filtro != null ? filtro.categoriaId() : null;
        Boolean estado = filtro != null ? filtro.estado() : null;

        LocalDateTime[] rango = requireRangoFechas(desde, hasta);
        LocalDateTime desdeConsulta = rango[0];
        LocalDateTime hastaConsulta = rango[1];
        String categoria = resolveCategoriaNombre(categoriaId);

        List<Map<String, Object>> raw = detalleOrdenVentaRepository.getVentasPorProductoAbc(
                desdeConsulta,
                hastaConsulta,
                EnumCodigoEstado.ANULADA.getCodigo(),
                categoriaId,
                estado
                );

        BigDecimal totalVentas = BigDecimal.ZERO;
        for (Map<String, Object> row : raw) {
            totalVentas = totalVentas.add(toBigDecimal(row.get("valorVentas")));
        }

        BigDecimal acumulado = BigDecimal.ZERO;
        List<Map<String, Object>> data = new ArrayList<>();

        for (Map<String, Object> row : raw) {
            String sku = Objects.toString(row.get("sku"), null);
            String nombre = Objects.toString(row.get("nombre"), null);
            String categoriaItem = Objects.toString(row.get("categoria"), null);
            Integer cantidadVendida = toInteger(row.get("cantidadVendida"));
            BigDecimal precioUnitario = toBigDecimal(row.get("precioUnitario"));
            BigDecimal valorVentas = toBigDecimal(row.get("valorVentas"));

            BigDecimal porcentaje = totalVentas.signum() == 0
                    ? BigDecimal.ZERO
                    : valorVentas.divide(totalVentas, 8, RoundingMode.HALF_UP);

            acumulado = acumulado.add(porcentaje);

            String clase = acumulado.compareTo(UMBRAL_A) <= 0
                    ? "A"
                    : (acumulado.compareTo(UMBRAL_B) <= 0 ? "B" : "C");

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("sku", sku);
            item.put("nombre", nombre);
            item.put("categoria", categoriaItem);
            item.put("cantidadVendida", cantidadVendida);
            item.put("precioUnitario", precioUnitario);
            item.put("valorVentas", valorVentas);
            item.put("porcentaje", porcentaje.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
            item.put("porcentajeAcumulado",
                    acumulado.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
            item.put("clase", clase);
            data.add(item);
        }

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("categoria", categoria);
        resumen.put("estado", estado);
        resumen.put("totalItems", data.size());
        resumen.put("valorTotal", totalVentas);
        resumen.put("moneda", MONEDA_DEFAULT);

        String titulo = "Análisis Pareto ABC - Ventas - Categoría: " + categoria + " (" +
                desdeConsulta.format(TITULO_DATE_TIME_FORMAT) +
                " a " +
                hastaConsulta.format(TITULO_DATE_TIME_FORMAT) +
                ")";
        return buildReport(titulo, resumen, data);
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteResponseDto stockActualAlertas(ReporteStockAlertasFiltroRequest filtro) {
        Integer categoriaId = filtro != null ? filtro.categoriaId() : null;
        Boolean estado = filtro != null ? filtro.estado() : null;
        Boolean bajoMinimo = filtro != null && Boolean.TRUE.equals(filtro.bajoMinimo());

        String categoria = resolveCategoriaNombre(categoriaId);

        List<Map<String, Object>> raw = inventarioItemRepository.getStockAlertas(categoriaId, estado, bajoMinimo);

        int enRiesgo = 0;
        int sinStock = 0;
        List<Map<String, Object>> data = new ArrayList<>();

        for (Map<String, Object> row : raw) {

            Integer id = toInteger(row.get("id"));
            String sku = Objects.toString(row.get("sku"), null);
            String nombre = Objects.toString(row.get("nombre"), null);
            Integer categoriaItemId = toInteger(row.get("categoriaId"));
            String categoriaItem = Objects.toString(row.get("categoria"), null);

            Integer stock = Objects.requireNonNullElse(toInteger(row.get("stock")), 0);
            Integer reservado = Objects.requireNonNullElse(toInteger(row.get("reservado")), 0);
            Integer disponible = Objects.requireNonNullElse(toInteger(row.get("stockDisponible")), 0);
            Integer cantidadMinima = Objects.requireNonNullElse(toInteger(row.get("cantidadMinima")), 0);
            LocalDateTime fechaUltimoMov = toLocalDateTime(row.get("fechaUltimoMovimiento"));

            String estadoAbastecimiento;
            if (disponible == 0) {
                estadoAbastecimiento = "Agotado";
                enRiesgo++;
                sinStock++;
            } else if (cantidadMinima > 0 && disponible <= cantidadMinima) {
                estadoAbastecimiento = "Critico";
                enRiesgo++;
            } else if (cantidadMinima > 0 && disponible <= cantidadMinima * 2) {
                estadoAbastecimiento = "Stock Bajo";
                enRiesgo++;
            } else if (cantidadMinima > 0 && disponible > cantidadMinima * 3) {
                estadoAbastecimiento = "Sobrestock";
            } else {
                estadoAbastecimiento = "Optimo";
            }

            boolean alerta = !estadoAbastecimiento.equals("Optimo")
                    && !estadoAbastecimiento.equals("Sobrestock");

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", id);
            item.put("sku", sku);
            item.put("nombre", nombre);
            item.put("categoriaId", categoriaItemId);
            item.put("categoria", categoriaItem);
            item.put("stock", stock);
            item.put("reservado", reservado);
            item.put("disponible", disponible);
            item.put("stockMinimo", cantidadMinima);
            item.put("estadoAbastecimiento", estadoAbastecimiento);
            item.put("alerta", alerta);
            item.put("fechaUltimoMovimiento", fechaUltimoMov);
            data.add(item);
        }

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("categoriaId", categoriaId);
        resumen.put("categoria", categoria);
        resumen.put("estado", estado);
        resumen.put("bajoMinimo", bajoMinimo);
        resumen.put("totalItems", data.size());
        resumen.put("moneda", MONEDA_DEFAULT);
        resumen.put("productosEnRiesgo", enRiesgo);
        resumen.put("productosSinStock", sinStock);

        return buildReport("Stock Actual y Alertas - " + categoria, resumen, data);
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteResponseDto kardexPorProducto(
            ReporteKardexFiltroRequest filtro) {

        LocalDateTime desde = filtro != null ? filtro.desde() : null;
        LocalDateTime hasta = filtro != null ? filtro.hasta() : null;
        String tipoMovimiento = filtro != null ? filtro.tipoMovimiento() : null;
        Integer productoId = filtro != null ? filtro.productoId() : null;

        LocalDateTime[] rango = requireRangoFechas(desde, hasta);
        LocalDateTime desdeConsulta = rango[0];
        LocalDateTime hastaConsulta = rango[1];

        Producto producto = productoRepository
                .findById(Objects.requireNonNull(productoId, "productoId must not be null"))
                .orElse(null);

        String tituloProducto = producto != null
                ? (producto.getCodigo() + " - " + producto.getNombre())
                : ("Producto " + productoId);

        Integer saldoInicialRaw = movimientoInventarioRepository
                .getSaldoAntesDe(productoId, desdeConsulta);
        int saldoAcumulado = saldoInicialRaw != null ? saldoInicialRaw : 0;

        List<Map<String, Object>> raw = movimientoInventarioRepository.getKardexPorProducto(
                productoId,
                tipoMovimiento,
                desdeConsulta,
                hastaConsulta);

        int totalEntradas = 0;
        int totalSalidas = 0;

        List<Map<String, Object>> data = new ArrayList<>();
        for (Map<String, Object> row : raw) {
            String tipo = Objects.toString(row.get("tipoMovimiento"), null);
            Integer cantidad = toInteger(row.get("cantidad"));

            int entradas = 0;
            int salidas = 0;
            if (tipo != null && cantidad != null) {
                if (isEntrada(tipo)) {
                    entradas = cantidad;
                    saldoAcumulado += cantidad;
                    totalEntradas += cantidad;
                } else if (isSalida(tipo)) {
                    salidas = cantidad;
                    saldoAcumulado -= cantidad;
                    totalSalidas += cantidad;
                }
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("fechaMovimiento", row.get("fechaMovimiento"));
            item.put("tipoMovimiento", tipo);
            item.put("cantidad", cantidad);
            item.put("entradas", entradas);
            item.put("salidas", salidas);
            item.put("saldoAcumulado", saldoAcumulado);
            item.put("ubicacion", row.get("ubicacion"));
            item.put("usuario", row.get("usuario"));
            item.put("motivo", row.get("motivo"));
            item.put("referencia", row.get("referencia"));
            item.put("notas", row.get("notas"));
            data.add(item);
        }

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("producto", tituloProducto);
        resumen.put("totalItems", data.size());
        resumen.put("saldoInicial", saldoInicialRaw != null ? saldoInicialRaw : 0);
        resumen.put("totalEntradas", totalEntradas);
        resumen.put("totalSalidas", totalSalidas);
        resumen.put("saldoFinal", saldoAcumulado);
        resumen.put("moneda", MONEDA_DEFAULT);

        String titulo = "Kardex por Producto - " + tituloProducto + " (" +
                desdeConsulta.format(TITULO_DATE_TIME_FORMAT) +
                " a " +
                hastaConsulta.format(TITULO_DATE_TIME_FORMAT) + ")";

        return buildReport(titulo, resumen, data);

    }

    @Override
    @Transactional(readOnly = true)
    public ReporteResponseDto analisisRotacion(ReporteRotacionFiltroRequest filtro) {
        LocalDateTime desde = filtro != null ? filtro.desde() : null;
        LocalDateTime hasta = filtro != null ? filtro.hasta() : null;
        Integer categoriaId = filtro != null ? filtro.categoriaId() : null;
        Boolean estado = filtro != null ? filtro.estado() : null;

        LocalDateTime[] rango = requireRangoFechas(desde, hasta);
        LocalDateTime desdeConsulta = rango[0];
        LocalDateTime hastaConsulta = rango[1];

        String categoria = resolveCategoriaNombre(categoriaId);

        long diasPeriodo = Math.max(1, ChronoUnit.DAYS.between(desdeConsulta, hastaConsulta));
        BigDecimal mesesPeriodo = BigDecimal.valueOf(diasPeriodo)
                .divide(BigDecimal.valueOf(30), 4, RoundingMode.HALF_UP)
                .max(BigDecimal.ONE);

        List<Map<String, Object>> ventasRaw = detalleOrdenVentaRepository
                .getCantidadVentasPorProducto(
                        desdeConsulta,
                        hastaConsulta,
                        EnumCodigoEstado.ANULADA.getCodigo(),
                        categoriaId,
                        estado);

        Map<Integer, Integer> ventasMap = new HashMap<>();
        for (Map<String, Object> row : ventasRaw) {
            Integer id = toInteger(row.get("id"));
            Integer total = toInteger(row.get("cantidadVendida"));
            if (id != null)
                ventasMap.put(id, total != null ? total : 0);
        }

        List<Map<String, Object>> invRaw = inventarioItemRepository
                .getStockActualPorProducto(categoriaId, estado);

        Map<Integer, Map<String, Object>> invMap = new HashMap<>();
        for (Map<String, Object> row : invRaw) {
            Integer id = toInteger(row.get("id"));
            if (id != null)
                invMap.put(id, row);
        }

        List<Producto> productos = productoRepository.findForReporte(categoriaId, estado);

        int huesos = 0;
        List<Map<String, Object>> data = new ArrayList<>();

        for (Producto p : productos) {
            Map<String, Object> inv = invMap.get(p.getId());

            int stockVal = Objects.requireNonNullElse(
                    inv != null ? toInteger(inv.get("stock")) : 0, 0);
            int reservadoVal = Objects.requireNonNullElse(
                    inv != null ? toInteger(inv.get("reservado")) : 0, 0);
            int disponible = Math.max(0, stockVal - reservadoVal);
            int cantidadVendida = ventasMap.getOrDefault(p.getId(), 0);

            BigDecimal rotacionMensual = BigDecimal.valueOf(cantidadVendida)
                    .divide(mesesPeriodo, 2, RoundingMode.HALF_UP);

            String clasificacion;
            if (cantidadVendida == 0) {
                clasificacion = "HUESO";
                huesos++;
            } else if (rotacionMensual.compareTo(new BigDecimal("20")) >= 0) {
                clasificacion = "ALTA";
            } else if (rotacionMensual.compareTo(new BigDecimal("5")) >= 0) {
                clasificacion = "MEDIA";
            } else {
                clasificacion = "BAJA";
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("sku", p.getCodigo());
            item.put("nombre", p.getNombre());
            item.put("categoria", p.getCategoria() != null ? p.getCategoria().getNombre() : null);
            item.put("stock", stockVal);
            item.put("reservado", reservadoVal);
            item.put("disponible", disponible);
            item.put("cantidadVendidaPeriodo", cantidadVendida);
            item.put("rotacionMensual", rotacionMensual);
            item.put("clasificacion", clasificacion);
            data.add(item);
        }

        data.sort((a, b) -> Integer.compare(
                Objects.requireNonNullElse(toInteger(b.get("cantidadVendidaPeriodo")), 0),
                Objects.requireNonNullElse(toInteger(a.get("cantidadVendidaPeriodo")), 0)));

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("categoria", categoria);
        resumen.put("estado", estado);
        resumen.put("diasPeriodo", diasPeriodo);
        resumen.put("mesesPeriodo", mesesPeriodo);
        resumen.put("totalItems", data.size());
        resumen.put("moneda", MONEDA_DEFAULT);
        resumen.put("productosHueso", huesos);

        String titulo = "Analisis de Rotacion - " + categoria + " (" +
                desdeConsulta.format(TITULO_DATE_TIME_FORMAT) +
                " a " +
                hastaConsulta.format(TITULO_DATE_TIME_FORMAT) + ")";

        return buildReport(titulo, resumen, data);

    }

    private static ReporteResponseDto buildReport(String nombreReporte, Map<String, Object> resumen,
            List<Map<String, Object>> data) {
        return new ReporteResponseDto(
                nombreReporte,
                LocalDateTime.now().withNano(0).format(TITULO_DATE_TIME_FORMAT),
                resumen,
                data);
    }

    private String resolveCategoriaNombre(Integer categoriaId) {
        if (categoriaId == null) {
            return "Todas";
        }
        return categoriaRepository.findById(categoriaId)
                .map(Categoria::getNombre)
                .orElse("Categoría");
    }

    private static boolean isEntrada(String tipo) {
        return EnumCodigoEstado.ENTRADA.name().equalsIgnoreCase(tipo)
                || EnumCodigoEstado.ENTRADA.getCodigo().equalsIgnoreCase(tipo)
                || EnumCodigoEstado.AJUSTE_ENTRADA_INVENTARIO.name().equalsIgnoreCase(tipo)
                || EnumCodigoEstado.AJUSTE_ENTRADA_INVENTARIO.getCodigo().equalsIgnoreCase(tipo);
    }

    private static boolean isSalida(String tipo) {
        return EnumCodigoEstado.SALIDA.name().equalsIgnoreCase(tipo)
                || EnumCodigoEstado.SALIDA.getCodigo().equalsIgnoreCase(tipo)
                || EnumCodigoEstado.TRANSFERENCIA.name().equalsIgnoreCase(tipo)
                || EnumCodigoEstado.TRANSFERENCIA.getCodigo().equalsIgnoreCase(tipo)
                || EnumCodigoEstado.AJUSTE_SALIDA_INVENTARIO.name().equalsIgnoreCase(tipo)
                || EnumCodigoEstado.AJUSTE_SALIDA_INVENTARIO.getCodigo().equalsIgnoreCase(tipo);
    }

    private static LocalDateTime[] requireRangoFechas(LocalDateTime desde, LocalDateTime hasta) {
        if (desde == null || hasta == null) {
            throw new BusinessException(
                    "400",
                    "Parámetros inválidos",
                    "Los parámetros 'desde' y 'hasta' son requeridos.",
                    TypeException.W);
        }

        LocalDateTime desdeConsulta = desde.withNano(0);
        LocalDateTime hastaConsulta = hasta.withNano(0);

        if (desdeConsulta.isAfter(hastaConsulta)) {
            LocalDateTime tmp = desdeConsulta;
            desdeConsulta = hastaConsulta;
            hastaConsulta = tmp;
        }

        return new LocalDateTime[] { desdeConsulta, hastaConsulta };
    }

    private static Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer i) {
            return i;
        }
        if (value instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        if (value instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private static LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime ldt) {
            return ldt;
        }
        if (value instanceof java.sql.Timestamp t) {
            return t.toLocalDateTime();
        }
        if (value instanceof java.util.Date d) {
            return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        }
        try {
            return LocalDateTime.parse(value.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
