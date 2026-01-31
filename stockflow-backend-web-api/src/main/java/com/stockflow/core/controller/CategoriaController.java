package com.stockflow.core.controller;

import com.stockflow.core.controller.contract.CategoriaControllerOpenApi;
import com.stockflow.core.dto.CategoriaDto;
import com.stockflow.core.entity.Categoria;
import com.stockflow.core.service.CategoriaService;
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
public class CategoriaController extends GenericController<CategoriaDto, Categoria, Integer> implements CategoriaControllerOpenApi {


    private final CategoriaService categoriaService;


    @Override
    public GenericCrud<CategoriaDto, Categoria, Integer> getCrud() {
        return categoriaService;
    }

    @Override
    protected Integer getPK(CategoriaDto categoriaDto) {
        return categoriaDto.getId();
    }


    @Override
    public ResponseEntity<ApiResponse> findById(Integer k) {
        CategoriaDto d = getCrud().findById(k);
        if (d != null) {
            WebMvcLinkBuilder linkTo = linkTo(methodOn(this.getClass()).findById(k));
            d.add(linkTo.withRel(Categoria.class.getSimpleName() + "-resource"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Obtener categoria", d));
    }

    @Override
    public ResponseEntity<ApiResponse> findAll() {

        return ResponseEntity.ok(ApiResponse.ok("Listado de categorias", categoriaService.findAll()));
    }
}
