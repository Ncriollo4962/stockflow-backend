package com.stockflow.core.repository;

import com.stockflow.core.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto,  Integer> {

    Optional<Producto> findByCodigo(String codigo);
    List<Producto> findByCategoriaId(Integer categoriaId);

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.estado = true AND (SELECT COALESCE(SUM(i.cantidad), 0) FROM InventarioItem i WHERE i.producto = p) < p.cantidadMinima")
    Long countProductosConStockCritico();

}
