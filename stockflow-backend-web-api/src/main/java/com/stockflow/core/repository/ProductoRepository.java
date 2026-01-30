package com.stockflow.core.repository;

import com.stockflow.core.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto,  Integer> {

    Optional<Producto> findByCodigo(String codigo);
    List<Producto> findByCategoriaId(Integer categoriaId);

}
