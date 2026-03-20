package com.stockflow.core.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.stockflow.core.controller.contract.UbicacionControllerOpenApi;
import com.stockflow.core.dto.UbicacionDto;
import com.stockflow.core.entity.Ubicacion;
import com.stockflow.core.service.UbicacionService;
import com.stockflow.core.utils.common.ApiResponse;
import com.stockflow.core.utils.common.GenericController;
import com.stockflow.core.utils.common.GenericCrud;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UbicacionController extends GenericController<UbicacionDto, Ubicacion, Integer>
        implements UbicacionControllerOpenApi {

    private final UbicacionService ubicacionService;

    @Override
    public GenericCrud<UbicacionDto, Ubicacion, Integer> getCrud() {
        return ubicacionService;
    }

    @Override
    protected Integer getPK(UbicacionDto dto) {
        return dto.getId();
    }

    @Override
    public ResponseEntity<ApiResponse> findById(Integer id) {
        UbicacionDto dto = ubicacionService.findById(id);
        if (dto != null) {
            WebMvcLinkBuilder linkTo = linkTo(methodOn(this.getClass()).findById(id));
            dto.add(linkTo.withRel(Ubicacion.class.getSimpleName() + "-resource"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Obtener ubicación", dto));
    }

    @Override
    public ResponseEntity<ApiResponse> findAll() {
        return ResponseEntity.ok(ApiResponse.ok("Listado de ubicaciones", ubicacionService.findAll()));
    }
}
