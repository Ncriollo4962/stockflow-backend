package com.stockflow.core.controller;

import com.stockflow.core.controller.contract.ProductoControllerOpenApi;
import com.stockflow.core.dto.ProductoDto;
import com.stockflow.core.entity.Producto;
import com.stockflow.core.service.ProductoService;
import com.stockflow.core.utils.common.ApiResponse;
import com.stockflow.core.utils.common.GenericController;
import com.stockflow.core.utils.common.GenericCrud;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
public class ProductoController extends GenericController<ProductoDto, Producto, Integer> implements ProductoControllerOpenApi {

    private final ProductoService productoService;
    @Override
    public GenericCrud<ProductoDto, Producto, Integer> getCrud() {
        return productoService;
    }

    @Override
    protected Integer getPK(ProductoDto productoDto) {
        return productoDto.getId();
    }

    @Override
    public ResponseEntity<ApiResponse> findById(Integer k) {
        ProductoDto d = getCrud().findById(k);
        if (d != null) {
            WebMvcLinkBuilder linkTo = linkTo(methodOn(this.getClass()).findById(k));
            d.add(linkTo.withRel(Producto.class.getSimpleName() + "-resource"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Obtener producto", d));
    }

    @Override
    public ResponseEntity<ApiResponse> findAll() {
        return ResponseEntity.ok(ApiResponse.ok("Listado de productos", productoService.findAll()));
    }

    @Override
    public ResponseEntity<ApiResponse> findByCodigo(String codigo) {
        return ResponseEntity.ok(ApiResponse.ok("Obtener producto por código", productoService.findAll()));
    }
}
