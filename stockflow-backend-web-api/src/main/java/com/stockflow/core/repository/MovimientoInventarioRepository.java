package com.stockflow.core.repository;

import java.util.List;
import java.util.Map;

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

}
