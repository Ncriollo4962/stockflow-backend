package com.stockflow.core.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.stockflow.core.controller.contract.InventarioItemControllerOpenApi;
import com.stockflow.core.dto.InventarioItemDto;
import com.stockflow.core.entity.InventarioItem;
import com.stockflow.core.service.InventarioItemService;
import com.stockflow.core.utils.common.ApiResponse;
import com.stockflow.core.utils.common.GenericController;
import com.stockflow.core.utils.common.GenericCrud;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class InventarioItemController extends GenericController<InventarioItemDto, InventarioItem, Integer>
        implements InventarioItemControllerOpenApi {

    private final InventarioItemService inventarioItemService;

    @Override
    public GenericCrud<InventarioItemDto, InventarioItem, Integer> getCrud() {
        return inventarioItemService;
    }

    @Override
    protected Integer getPK(InventarioItemDto dto) {
        return dto.getId();
    }

    @Override
    public ResponseEntity<ApiResponse> findById(Integer id) {
        InventarioItemDto dto = inventarioItemService.findById(id);
        if (dto != null) {
            WebMvcLinkBuilder linkTo = linkTo(methodOn(this.getClass()).findById(id));
            dto.add(linkTo.withRel(InventarioItem.class.getSimpleName() + "-resource"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Obtener item de inventario", dto));
    }

    @Override
    public ResponseEntity<ApiResponse> findAll() {
        return ResponseEntity.ok(ApiResponse.ok("Listado de items de inventario", inventarioItemService.findAll()));
    }
}
