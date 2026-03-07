package com.stockflow.core.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.stockflow.core.controller.contract.OrdenVentaControllerOpenApi;
import com.stockflow.core.dto.OrdenVentaDto;
import com.stockflow.core.entity.OrdenVenta;
import com.stockflow.core.service.OrdenVentaService;
import com.stockflow.core.utils.common.ApiResponse;
import com.stockflow.core.utils.common.GenericController;
import com.stockflow.core.utils.common.GenericCrud;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrdenVentaController extends GenericController<OrdenVentaDto, OrdenVenta, Integer>
        implements OrdenVentaControllerOpenApi {

    private final OrdenVentaService ordenVentaService;

    @Override
    public GenericCrud<OrdenVentaDto, OrdenVenta, Integer> getCrud() {
        return ordenVentaService;
    }

    @Override
    protected Integer getPK(OrdenVentaDto detalleOrdenVenta) {
        return detalleOrdenVenta.getId();
    }

    @Override
    public ResponseEntity<ApiResponse> findById(Integer k) {
        OrdenVentaDto d = getCrud().findById(k);
        if (d != null) {
            WebMvcLinkBuilder linkTo = linkTo(methodOn(this.getClass()).findById(k));
            d.add(linkTo.withRel(OrdenVenta.class.getSimpleName() + "-resource"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Obtener orden de venta", d));
    }

    @Override
    public ResponseEntity<ApiResponse> findAll() {
        return ResponseEntity.ok(ApiResponse.ok("Listado de ordenes de venta", ordenVentaService.findAll()));
    }

    @Override
    public ResponseEntity<ApiResponse> generateNumeroOrden() {
        return ResponseEntity.ok(ApiResponse.ok("Número de orden generado", ordenVentaService.generateNumeroOrden()));
    }

    @Override
    public ResponseEntity<ApiResponse> findPendientesDespacho() {
        return ResponseEntity.ok(ApiResponse.ok("Listado de ordenes de venta pendientes de despacho", ordenVentaService.findPendientesDespacho()));
    }
}
