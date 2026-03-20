package com.stockflow.core.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.stockflow.core.controller.contract.ProveedorControllerOpenApi;
import com.stockflow.core.dto.ProveedorDto;
import com.stockflow.core.entity.Proveedor;
import com.stockflow.core.service.ProveedorService;
import com.stockflow.core.utils.common.ApiResponse;
import com.stockflow.core.utils.common.GenericController;
import com.stockflow.core.utils.common.GenericCrud;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ProveedorController extends GenericController<ProveedorDto, Proveedor, Integer>
        implements ProveedorControllerOpenApi {

    private final ProveedorService proveedorService;

    @Override
    public GenericCrud<ProveedorDto, Proveedor, Integer> getCrud() {
        return proveedorService;
    }

    @Override
    protected Integer getPK(ProveedorDto proveedor) {
        return proveedor.getId();
    }

    @Override
    public ResponseEntity<ApiResponse> findById(Integer k) {
        ProveedorDto d = getCrud().findById(k);
        if (d != null) {
            WebMvcLinkBuilder linkTo = linkTo(methodOn(this.getClass()).findById(k));
            d.add(linkTo.withRel(Proveedor.class.getSimpleName() + "-resource"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Obtener proveedor", d));
    }

    @Override
    public ResponseEntity<ApiResponse> findAll() {
        return ResponseEntity.ok(ApiResponse.ok("Listado de proveedores", proveedorService.findAll()));
    }

}
