package com.stockflow.core.controller.contract;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.stockflow.core.dto.MovimientoInventarioDto;
import com.stockflow.core.utils.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RequestMapping("/api/movimientos")
public interface MovimientoInventarioControllerOpenApi {

    @Operation(summary = "Obtiene un movimiento por su ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MovimientoInventarioDto.class)))
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> findById(@PathVariable("id") Integer id);

    @Operation(summary = "Lista todos los movimientos registrados")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MovimientoInventarioDto.class)))
    @GetMapping(path = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> findAll();

    @Operation(summary = "Registra un nuevo movimiento de inventario")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Movimiento registrado con éxito")
    @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> insert(@RequestBody MovimientoInventarioDto dto);

    @Operation(summary = "Registra una lista de nuevos movimientos de inventario")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Movimientos registrados con éxito")
    @PostMapping(path = "/register-all", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> insertAll(@RequestBody java.util.List<MovimientoInventarioDto> dtos);

    @Operation(summary = "Actualiza los datos de un movimiento existente")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Movimiento actualizado con éxito")
    @PutMapping(path = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> update(@RequestBody MovimientoInventarioDto dto);

    @Operation(summary = "Eliminar un movimiento")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Movimiento eliminado con éxito")
    @DeleteMapping(path = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> delete(@PathVariable("id") Integer id);
}
