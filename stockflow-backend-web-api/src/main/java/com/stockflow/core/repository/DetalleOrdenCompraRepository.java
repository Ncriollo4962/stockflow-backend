package com.stockflow.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.stockflow.core.entity.DetalleOrdenCompra;

public interface DetalleOrdenCompraRepository extends JpaRepository<DetalleOrdenCompra, Integer> {
    @Modifying
    @Query("DELETE FROM DetalleOrdenCompra d WHERE d.ordenCompra.id = :ordenCompraId")
    void deleteByOrdenCompraId(@Param("ordenCompraId") Integer ordenCompraId);

}
