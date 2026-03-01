package com.stockflow.core.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.stockflow.core.entity.InventarioItem;

public interface InventarioItemRepository extends JpaRepository<InventarioItem, Integer> {
    
    @Query("SELECT c.nombre as categoria, SUM(i.cantidad * p.precioCosto) as valorTotal " +
           "FROM InventarioItem i " +
           "JOIN i.producto p " +
           "JOIN p.categoria c " +
           "GROUP BY c.nombre")
    List<Map<String, Object>> getValorizacionInventarioPorCategoria();

}
