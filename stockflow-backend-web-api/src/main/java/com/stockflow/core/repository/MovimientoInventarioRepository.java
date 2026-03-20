package com.stockflow.core.repository;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.stockflow.core.entity.MovimientoInventario;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Integer> {
    
    @Query("SELECT FUNCTION('MONTH', m.fechaMovimiento) as mes, " +
           "SUM(CASE WHEN m.tipoMovimiento = 'ENTRADA' THEN m.cantidad ELSE 0 END) as totalEntradas, " +
           "SUM(CASE WHEN m.tipoMovimiento = 'SALIDA' THEN m.cantidad ELSE 0 END) as totalSalidas " +
           "FROM MovimientoInventario m " +
           "WHERE FUNCTION('YEAR', m.fechaMovimiento) = :anio " +
           "GROUP BY FUNCTION('MONTH', m.fechaMovimiento) " +
           "ORDER BY FUNCTION('MONTH', m.fechaMovimiento)")
    List<Map<String, Object>> getMovimientosPorMes(@Param("anio") Integer anio);

    @Query("SELECT m.producto.nombre as producto, SUM(m.cantidad) as totalSalidas " +
           "FROM MovimientoInventario m " +
           "WHERE m.tipoMovimiento = 'SALIDA' AND FUNCTION('YEAR', m.fechaMovimiento) = :anio " +
           "GROUP BY m.producto.nombre " +
           "ORDER BY totalSalidas DESC")
    List<Map<String, Object>> getTopProductosSalidas(@Param("anio") Integer anio);

    @Query("""
    SELECT 
        SUM(CASE 
            WHEN m.tipoMovimiento IN ('ENTRADA', 'AJUSTE_ENTRADA_INVENTARIO') 
                THEN m.cantidad
            WHEN m.tipoMovimiento IN ('SALIDA', 'TRANSFERENCIA', 'AJUSTE_SALIDA_INVENTARIO') 
                THEN -m.cantidad
            ELSE 0
        END)
    FROM MovimientoInventario m
    WHERE m.producto.id = :productoId
      AND m.fechaMovimiento < :desde
    """)
Integer getSaldoAntesDe(
    @Param("productoId") Integer productoId,
    @Param("desde") LocalDateTime desde
);

@Query("""
    SELECT
        m.id                    as id,
        m.fechaMovimiento       as fechaMovimiento,
        m.tipoMovimiento        as tipoMovimiento,
        m.cantidad              as cantidad,
        u.id                    as ubicacionId,
        u.nombre                as ubicacion,
        us.id                   as usuarioId,
        us.nombre               as usuario,
        m.motivo                as motivo,
        m.referencia            as referencia,
        m.notas                 as notas
    FROM MovimientoInventario m
    JOIN m.ubicacion u
    JOIN m.usuario us
    WHERE m.producto.id = :productoId
      AND m.fechaMovimiento BETWEEN :desde AND :hasta
      AND (:tipoMovimiento IS NULL OR m.tipoMovimiento = :tipoMovimiento)
    ORDER BY m.fechaMovimiento ASC
    """)
List<Map<String, Object>> getKardexPorProducto(
        @Param("productoId") Integer productoId,
        @Param("tipoMovimiento") String tipoMovimiento,
        @Param("desde") LocalDateTime desde,
        @Param("hasta") LocalDateTime hasta
);

    @Query("""
            SELECT
                p.id as id,
                p.codigo as sku,
                p.nombre as nombre,
                SUM(m.cantidad) as totalSalidas
            FROM MovimientoInventario m
            JOIN m.producto p
            WHERE m.tipoMovimiento = :tipoSalida
              AND (:ubicacionId IS NULL OR m.ubicacion.id = :ubicacionId)
              AND m.fechaMovimiento BETWEEN :desde AND :hasta
            GROUP BY p.id, p.codigo, p.nombre
            ORDER BY SUM(m.cantidad) DESC
            """)
    List<Map<String, Object>> getSalidasPorProducto(
            @Param("tipoSalida") String tipoSalida,
            @Param("ubicacionId") Integer ubicacionId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta
    );

    @Query("""
            SELECT
                m.id as id,
                m.fechaMovimiento as fechaMovimiento,
                m.tipoMovimiento as tipoMovimiento,
                m.cantidad as cantidad,
                p.id as productoId,
                p.codigo as sku,
                p.nombre as producto,
                u.id as ubicacionId,
                u.nombre as ubicacion,
                us.id as usuarioId,
                us.email as usuario,
                m.motivo as motivo,
                m.referencia as referencia,
                m.notas as notas
            FROM MovimientoInventario m
            JOIN m.producto p
            JOIN m.ubicacion u
            JOIN m.usuario us
            WHERE m.tipoMovimiento IN (:tipos)
              AND (:productoId IS NULL OR p.id = :productoId)
              AND (:ubicacionId IS NULL OR u.id = :ubicacionId)
              AND (:usuarioId IS NULL OR us.id = :usuarioId)
              AND (:referencia IS NULL OR LOWER(COALESCE(m.referencia, '')) LIKE LOWER(CONCAT('%', :referencia, '%')))
              AND m.fechaMovimiento BETWEEN :desde AND :hasta
            ORDER BY m.fechaMovimiento DESC, m.id DESC
            """)
    List<Map<String, Object>> getAjustes(
            @Param("tipos") List<String> tipos,
            @Param("productoId") Integer productoId,
            @Param("ubicacionId") Integer ubicacionId,
            @Param("usuarioId") Integer usuarioId,
            @Param("referencia") String referencia,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta
    );

}
