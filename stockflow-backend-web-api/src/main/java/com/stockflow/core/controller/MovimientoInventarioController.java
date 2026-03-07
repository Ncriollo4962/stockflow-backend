package com.stockflow.core.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.stockflow.core.controller.contract.MovimientoInventarioControllerOpenApi;
import com.stockflow.core.dto.MovimientoInventarioDto;
import com.stockflow.core.entity.MovimientoInventario;
import com.stockflow.core.service.MovimientoInventarioService;
import com.stockflow.core.utils.common.ApiResponse;
import com.stockflow.core.utils.common.GenericController;
import com.stockflow.core.utils.common.GenericCrud;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MovimientoInventarioController extends GenericController<MovimientoInventarioDto, MovimientoInventario, Integer>
        implements MovimientoInventarioControllerOpenApi {

    private final MovimientoInventarioService movimientoInventarioService;

    @Override
    public GenericCrud<MovimientoInventarioDto, MovimientoInventario, Integer> getCrud() {
        return movimientoInventarioService;
    }

    @Override
    protected Integer getPK(MovimientoInventarioDto dto) {
        return dto.getId();
    }

    @Override
    public ResponseEntity<ApiResponse> findById(Integer id) {
        MovimientoInventarioDto dto = movimientoInventarioService.findById(id);
        if (dto != null) {
            WebMvcLinkBuilder linkTo = linkTo(methodOn(this.getClass()).findById(id));
            dto.add(linkTo.withRel(MovimientoInventario.class.getSimpleName() + "-resource"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Obtener movimiento", dto));
    }

    @Override
    public ResponseEntity<ApiResponse> findAll() {
        return ResponseEntity.ok(ApiResponse.ok("Listado de movimientos", movimientoInventarioService.findAll()));
    }

    @Override
    public ResponseEntity<ApiResponse> insert(MovimientoInventarioDto dto) {
        return ResponseEntity.status(201).body(ApiResponse.create("Movimiento registrado", movimientoInventarioService.insert(dto)));
    }

    @Override
    public ResponseEntity<ApiResponse> insertAll(java.util.List<MovimientoInventarioDto> dtos) {
        return ResponseEntity.status(201).body(ApiResponse.create("Movimientos registrados", movimientoInventarioService.insertAll(dtos)));
    }
}
