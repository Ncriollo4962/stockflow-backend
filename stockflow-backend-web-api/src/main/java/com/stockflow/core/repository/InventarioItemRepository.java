package com.stockflow.core.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.stockflow.core.entity.InventarioItem;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

public interface InventarioItemRepository extends JpaRepository<InventarioItem, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventarioItem i WHERE i.producto.id = :productoId AND i.ubicacion.id = :ubicacionId")
    List<InventarioItem> findForUpdateByProductoIdAndUbicacionId(Integer productoId, Integer ubicacionId);

    List<InventarioItem> findByProductoIdAndUbicacionId(Integer productoId, Integer ubicacionId);

    List<InventarioItem> findByProductoIdAndUbicacionIdAndLote(Integer productoId, Integer ubicacionId,
            String lote);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventarioItem i WHERE i.producto.id = :productoId ORDER BY i.cantidad DESC")
    List<InventarioItem> findForUpdateByProductoId(
            @org.springframework.data.repository.query.Param("productoId") Integer productoId);

    @Query("SELECT c.nombre as categoria, SUM(i.cantidad * p.precioCosto) as valorTotal " +
            "FROM InventarioItem i " +
            "JOIN i.producto p " +
            "JOIN p.categoria c " +
            "GROUP BY c.nombre")
    List<Map<String, Object>> getValorizacionInventarioPorCategoria();

    @Query("""
            SELECT
                p.id                                                    as id,
                p.codigo                                                as sku,
                p.nombre                                                as nombre,
                c.id                                                    as categoriaId,
                c.nombre                                                as categoria,
                SUM(i.cantidad)                                         as stock,
                SUM(i.cantidadReservada)                                as reservado,
                SUM(i.cantidad - i.cantidadReservada)                   as stockDisponible,
                p.precioCosto                                           as costoUnitario,
                p.precioVenta                                           as precioVenta,
                p.cantidadMinima                                        as cantidadMinima,
                MAX(i.fechaActualizacion)                               as fechaUltimoMovimiento,
                SUM(i.cantidad * p.precioCosto)                         as valorTotal,
                SUM((i.cantidad - i.cantidadReservada) * p.precioCosto) as valorDisponible
            FROM InventarioItem i
            JOIN i.producto p
            JOIN p.categoria c
            WHERE (:categoriaId IS NULL OR c.id = :categoriaId)
              AND (:estado IS NULL OR (p.estado = :estado AND c.estado = :estado))
            GROUP BY p.id, p.codigo, p.nombre, c.id, c.nombre,
                     p.precioCosto, p.precioVenta, p.cantidadMinima
            HAVING (:soloConStock = false OR SUM(i.cantidad) > 0)
            ORDER BY SUM(i.cantidad * p.precioCosto) DESC
            """)
    List<Map<String, Object>> getInventarioValorizadoPorProducto(
            @Param("categoriaId") Integer categoriaId,
            @Param("estado") Boolean estado,
            @Param("soloConStock") boolean soloConStock);

    @Query("""
            SELECT
                p.id                                  as id,
                p.codigo                              as sku,
                p.nombre                              as nombre,
                c.id                                  as categoriaId,
                c.nombre                              as categoria,
                SUM(i.cantidad)                       as stock,
                SUM(i.cantidadReservada)              as reservado,
                SUM(i.cantidad - i.cantidadReservada) as stockDisponible,
                p.cantidadMinima                      as cantidadMinima,
                MAX(i.fechaActualizacion)             as fechaUltimoMovimiento
                FROM InventarioItem i
                JOIN i.producto p
                JOIN p.categoria c
                WHERE (:categoriaId IS NULL OR c.id = :categoriaId)
                  AND (:estado IS NULL OR (p.estado = :estado AND c.estado = :estado))
                GROUP BY p.id, p.codigo, p.nombre, c.id, c.nombre, p.cantidadMinima
                HAVING (:bajoMinimo = false
            OR SUM(i.cantidad - i.cantidadReservada) <= p.cantidadMinima)
                  AND (:bajoMinimo = false
                       OR SUM(i.cantidad - i.cantidadReservada) <= p.cantidadMinima)
                ORDER BY SUM(i.cantidad - i.cantidadReservada) ASC
                """)
    List<Map<String, Object>> getStockAlertas(
            @Param("categoriaId") Integer categoriaId,
            @Param("estado") Boolean estado,
            @Param("bajoMinimo") boolean bajoMinimo);

    @Query("""
            SELECT
                p.id                             as id,
                SUM(i.cantidad)                  as stock,
                SUM(i.cantidadReservada)         as reservado,
                SUM(i.cantidad - i.cantidadReservada) as stockDisponible
            FROM InventarioItem i
            JOIN i.producto p
            JOIN p.categoria c
            WHERE (:categoriaId IS NULL OR c.id = :categoriaId)
              AND (:estado IS NULL OR (p.estado = :estado AND c.estado = :estado))
            GROUP BY p.id
            """)
    List<Map<String, Object>> getStockActualPorProducto(
            @Param("categoriaId") Integer categoriaId,
            @Param("estado") Boolean estado);

}
