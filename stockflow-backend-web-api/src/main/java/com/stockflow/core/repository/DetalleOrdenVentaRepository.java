package com.stockflow.core.repository;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.stockflow.core.entity.DetalleOrdenVenta;

public interface DetalleOrdenVentaRepository extends JpaRepository<DetalleOrdenVenta, Integer> {

    List<DetalleOrdenVenta> findByOrdenVentaId(Integer ordenVentaId);

    @Modifying
    @Query("DELETE FROM DetalleOrdenVenta d WHERE d.ordenVenta.id = :ordenVentaId")
    void deleteByOrdenVentaId(@Param("ordenVentaId") Integer ordenVentaId);

    @Query("""
            SELECT
                p.id                as id,
                p.codigo            as sku,
                p.nombre            as nombre,
                c.id                as categoriaId,
                c.nombre            as categoria,
                SUM(d.cantidad)     as cantidadVendida,
                d.precioUnitario    as precioUnitario,
                SUM(d.subtotal)     as valorVentas
            FROM DetalleOrdenVenta d
            JOIN d.ordenVenta o
            JOIN d.producto p
            JOIN p.categoria c
            WHERE o.fechaVenta BETWEEN :desde AND :hasta
              AND o.estado <> :estadoAnulado
              AND (:categoriaId IS NULL OR c.id = :categoriaId)
              AND (:estado IS NULL OR (p.estado = :estado AND c.estado = :estado))
            GROUP BY p.id, p.codigo, p.nombre, c.id, c.nombre, d.precioUnitario
            ORDER BY SUM(d.subtotal) DESC
            """)
    List<Map<String, Object>> getVentasPorProductoAbc(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("estadoAnulado") String estadoAnulado,
            @Param("categoriaId") Integer categoriaId,
            @Param("estado") Boolean estado);

    @Query("""
            SELECT
                p.id                as id,
                p.codigo            as sku,
                p.nombre            as nombre,
                c.id                as categoriaId,
                c.nombre            as categoria,
                SUM(d.cantidad)     as cantidadVendida
            FROM DetalleOrdenVenta d
            JOIN d.ordenVenta o
            JOIN d.producto p
            JOIN p.categoria c
            WHERE o.fechaVenta BETWEEN :desde AND :hasta
              AND o.estado <> :estadoAnulado
              AND (:categoriaId IS NULL OR c.id = :categoriaId)
              AND (:estado IS NULL OR (p.estado = :estado AND c.estado = :estado))
            GROUP BY p.id, p.codigo, p.nombre, c.id, c.nombre
            ORDER BY SUM(d.cantidad) DESC
            """)
    List<Map<String, Object>> getCantidadVentasPorProducto(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("estadoAnulado") String estadoAnulado,
            @Param("categoriaId") Integer categoriaId,
            @Param("estado") Boolean estado);

}
