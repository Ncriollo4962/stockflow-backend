package com.stockflow.core.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.stockflow.core.entity.OrdenVenta;

@Repository
public interface OrdenVentaRepository extends JpaRepository<OrdenVenta, Integer> {

    Long countByEstado(String estado);

    @Query("SELECT FUNCTION('MONTH', o.fechaVenta) as mes, SUM(o.totalVenta) as total FROM OrdenVenta o WHERE FUNCTION('YEAR', o.fechaVenta) = :anio GROUP BY FUNCTION('MONTH', o.fechaVenta) ORDER BY FUNCTION('MONTH', o.fechaVenta)")
    List<Map<String, Object>> getVentasPorMes(@Param("anio") Integer anio);
}
