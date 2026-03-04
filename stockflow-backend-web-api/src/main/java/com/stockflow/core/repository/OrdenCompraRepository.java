package com.stockflow.core.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.stockflow.core.entity.OrdenCompra;

@Repository
public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Integer> {
    Long countByEstado(String estado);

    @Query("SELECT FUNCTION('MONTH', o.fechaOrdenCompra) as mes, SUM(o.totalCompra) as total FROM OrdenCompra o WHERE FUNCTION('YEAR', o.fechaOrdenCompra) = :anio GROUP BY FUNCTION('MONTH', o.fechaOrdenCompra) ORDER BY FUNCTION('MONTH', o.fechaOrdenCompra)")
    List<Map<String, Object>> getComprasPorMes(@Param("anio") Integer anio);
}
