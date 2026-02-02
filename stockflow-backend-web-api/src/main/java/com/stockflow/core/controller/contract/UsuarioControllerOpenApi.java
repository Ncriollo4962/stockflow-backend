package com.stockflow.core.controller.contract;

import com.stockflow.core.dto.UsuarioDto;
import com.stockflow.core.utils.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/usuarios")
public interface UsuarioControllerOpenApi {

    @Operation(summary = "Obtiene un usuario por su ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UsuarioDto.class)))
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> findById(@PathVariable("id") Integer k);

    @Operation(summary = "Lista todos los usuarios registrados")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UsuarioDto.class)))
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> findAll();

    @Operation(summary = "Registra un nuevo usuario")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Usuario registrado con éxito")
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> insert(@RequestBody UsuarioDto d);

    @Operation(summary = "Actualiza los datos de un usuario existente")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario actualizado con éxito")
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> update(@RequestBody UsuarioDto d);

    @Operation(summary = "Eliminar un usuario")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario eliminado con éxito")
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> delete(@RequestBody UsuarioDto d);

    @Operation(summary = "Busca un usuario por su nombre de usuario (email)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Buscar Usuario por email con éxito")
    @GetMapping(path = "/findByNameUser/{email}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> findByNameUser(@PathVariable("email") String email);
}
