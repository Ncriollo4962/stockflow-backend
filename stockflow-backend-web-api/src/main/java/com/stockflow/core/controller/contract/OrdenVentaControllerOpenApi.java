package com.stockflow.core.controller.contract;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.stockflow.core.dto.OrdenVentaDto;
import com.stockflow.core.utils.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RequestMapping("/api/ordenesventa")
public interface OrdenVentaControllerOpenApi {
    @Operation(summary = "Obtiene una orden de venta por su ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = OrdenVentaDto.class)))
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> findById(@PathVariable("id") Integer k);

    @Operation(summary = "Lista todas las ordenes de venta registradas")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = OrdenVentaDto.class)))
    @GetMapping(path = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> findAll();

    @Operation(summary = "Registra una nueva orden de venta")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Orden de venta registrada con éxito")
    @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> insert(@RequestBody OrdenVentaDto d);

    @Operation(summary = "Actualiza los datos de una orden de venta existente")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Orden de venta actualizada con éxito")
    @PutMapping(path = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> update(@RequestBody OrdenVentaDto d);

    @Operation(summary = "Eliminar una orden de venta")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Orden de venta eliminada con éxito")
    @DeleteMapping(path = "/delete/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> delete(@PathVariable("id") Integer k);

    @Operation(summary = "Genera un nuevo número de orden de venta")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = String.class)))
    @GetMapping(path = "/generate-number", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> generateNumeroOrden();
    
    @Operation(summary = "Lista las órdenes de venta pendientes de despacho")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = OrdenVentaDto.class)))
    @GetMapping(path = "/pendientes-despacho", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> findPendientesDespacho();

}
