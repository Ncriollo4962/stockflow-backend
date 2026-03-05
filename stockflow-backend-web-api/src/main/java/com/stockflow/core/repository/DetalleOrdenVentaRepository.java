package com.stockflow.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.stockflow.core.entity.DetalleOrdenVenta;

public interface DetalleOrdenVentaRepository extends JpaRepository<DetalleOrdenVenta, Integer> {
    @Modifying
    @Query("DELETE FROM DetalleOrdenVenta d WHERE d.ordenVenta.id = :ordenVentaId")
    void deleteByOrdenVentaId(@Param("ordenVentaId") Integer ordenVentaId);

}
