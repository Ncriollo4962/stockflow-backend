package com.stockflow.core.controller.contract;


import com.stockflow.core.dto.CategoriaDto;
import com.stockflow.core.utils.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/categorias")
public interface CategoriaControllerOpenApi {

    @Operation(summary = "Obtiene una categoría por su ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CategoriaDto.class)))
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> findById(@PathVariable("id") Integer k);

    @Operation(summary = "Lista todas las categorías")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CategoriaDto.class)))
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> findAll();

    @Operation(summary = "Registra una nueva categoría")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Categoría creada con éxito")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> insert(@RequestBody CategoriaDto d);

    @Operation(summary = "Actualiza una categoría existente")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Categoría actualizada con éxito")
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> update(@RequestBody CategoriaDto d);

    @Operation(summary = "Elimina una categoría")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Categoría eliminada con éxito")
    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> delete(@RequestBody CategoriaDto d);
}
