package com.stockflow.core.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.stockflow.core.entity.InventarioItem;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

public interface InventarioItemRepository extends JpaRepository<InventarioItem, Integer> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventarioItem i WHERE i.producto.id = :productoId AND i.ubicacion.id = :ubicacionId")
    List<InventarioItem> findForUpdateByProductoIdAndUbicacionId(Integer productoId, Integer ubicacionId);

    List<InventarioItem> findByProductoIdAndUbicacionId(Integer productoId, Integer ubicacionId);

    List<InventarioItem> findByProductoIdAndUbicacionIdAndLote(Integer productoId, Integer ubicacionId, String lote);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventarioItem i WHERE i.producto.id = :productoId ORDER BY i.cantidad DESC")
    List<InventarioItem> findForUpdateByProductoId(@org.springframework.data.repository.query.Param("productoId") Integer productoId);

    @Query("SELECT c.nombre as categoria, SUM(i.cantidad * p.precioCosto) as valorTotal " +
           "FROM InventarioItem i " +
           "JOIN i.producto p " +
           "JOIN p.categoria c " +
           "GROUP BY c.nombre")
    List<Map<String, Object>> getValorizacionInventarioPorCategoria();

}
