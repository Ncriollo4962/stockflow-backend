package com.stockflow.core.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.stockflow.core.controller.contract.OrdenCompraControllerOpenApi;
import com.stockflow.core.dto.OrdenCompraDto;
import com.stockflow.core.entity.OrdenCompra;
import com.stockflow.core.service.OrdenCompraService;
import com.stockflow.core.utils.common.ApiResponse;
import com.stockflow.core.utils.common.GenericController;
import com.stockflow.core.utils.common.GenericCrud;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrdenCompraController extends GenericController<OrdenCompraDto, OrdenCompra, Integer>
        implements OrdenCompraControllerOpenApi {

    private final OrdenCompraService ordenCompraService;

    @Override
    public GenericCrud<OrdenCompraDto, OrdenCompra, Integer> getCrud() {
        return ordenCompraService;
    }

    @Override
    protected Integer getPK(OrdenCompraDto detalleOrdenCompra) {
        return detalleOrdenCompra.getId();
    }

    @Override
    public ResponseEntity<ApiResponse> findById(Integer k) {
        OrdenCompraDto d = getCrud().findById(k);
        if (d != null) {
            WebMvcLinkBuilder linkTo = linkTo(methodOn(this.getClass()).findById(k));
            d.add(linkTo.withRel(OrdenCompra.class.getSimpleName() + "-resource"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Obtener orden de compra", d));
    }

    @Override
    public ResponseEntity<ApiResponse> findAll() {
        return ResponseEntity.ok(ApiResponse.ok("Listado de ordenes de compra", ordenCompraService.findAll()));
    }

    @Override
    public ResponseEntity<ApiResponse> generateNumeroOrden() {
        return ResponseEntity.ok(ApiResponse.ok("Número de orden generado", ordenCompraService.generateNumeroOrden()));
    }
}
