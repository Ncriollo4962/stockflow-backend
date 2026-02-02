package com.stockflow.core.controller;

import com.stockflow.core.controller.contract.UsuarioControllerOpenApi;
import com.stockflow.core.dto.UsuarioDto;
import com.stockflow.core.entity.Usuario;
import com.stockflow.core.service.UsuarioService;
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
public class UsuarioController extends GenericController<UsuarioDto, Usuario, Integer> implements UsuarioControllerOpenApi {

    private final UsuarioService usuarioService;

    @Override
    public GenericCrud<UsuarioDto, Usuario, Integer> getCrud() {
        return usuarioService;
    }

    @Override
    protected Integer getPK(UsuarioDto usuarioDto) {
        return usuarioDto.getId();
    }

    @Override
    public ResponseEntity<ApiResponse> findById(Integer k) {
        UsuarioDto d = getCrud().findById(k);
        if (d != null) {
            WebMvcLinkBuilder linkTo = linkTo(methodOn(this.getClass()).findById(k));
            d.add(linkTo.withRel(Usuario.class.getSimpleName() + "-resource"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Obtener usuario", d));
    }

    @Override
    public ResponseEntity<ApiResponse> findAll() {
        return ResponseEntity.ok(ApiResponse.ok("Listado de usuarios", usuarioService.findAll()));
    }

    @Override
    public ResponseEntity<ApiResponse> findByNameUser(String email) {
        return ResponseEntity.ok(ApiResponse.ok("Obtener usuario por email", usuarioService.findByNameUser(email)));
    }
}
