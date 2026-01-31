package com.stockflow.core.controller.contract;

import com.stockflow.core.dto.ProductoDto;
import com.stockflow.core.utils.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/productos")
public interface ProductoControllerOpenApi {

    @Operation(summary = "Obtiene un producto por su ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProductoDto.class)))
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> findById(@PathVariable("id") Integer k);

    @Operation(summary = "Lista todos los productos registrados")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProductoDto.class)))
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> findAll();

    @Operation(summary = "Registra un nuevo producto")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Producto registrado con éxito")
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> insert(@RequestBody ProductoDto d);

    @Operation(summary = "Actualiza los datos de un producto existente")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Producto actualizado con éxito")
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> update(@RequestBody ProductoDto d);

    @Operation(summary = "Eliminar un producto")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Producto eliminado con éxito")
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> delete(@RequestBody ProductoDto d);

    @Operation(summary = "Busca un producto por su código")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Buscar Producto por código con éxito")
    @GetMapping(path = "/findByCodigo/{codigo}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> findByCodigo(@PathVariable("codigo") String codigo);
}
