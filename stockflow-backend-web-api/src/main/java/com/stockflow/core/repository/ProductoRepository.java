package com.stockflow.core.repository;

import com.stockflow.core.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto,  Integer> {

    Optional<Producto> findByCodigo(String codigo);
    List<Producto> findByCategoriaId(Integer categoriaId);

    @Query("""
            SELECT p
            FROM Producto p
            JOIN FETCH p.categoria c
            WHERE (:categoriaId IS NULL OR c.id = :categoriaId)
              AND (:estado IS NULL OR (p.estado = :estado AND c.estado = :estado))
            ORDER BY p.nombre ASC
            """)
    List<Producto> findForReporte(@Param("categoriaId") Integer categoriaId, @Param("estado") Boolean estado);

    @Query("""
        SELECT COUNT(DISTINCT p.id)
        FROM Producto p
        WHERE p.estado = true
          AND (
              (SELECT COALESCE(SUM(i.cantidad), 0)
               FROM InventarioItem i
               WHERE i.producto = p) = 0
              OR
              (SELECT COALESCE(SUM(i.cantidad), 0)
               FROM InventarioItem i
               WHERE i.producto = p) <= p.cantidadMinima * 2
          )
        """)
    Long countProductosConStockCritico();

}
