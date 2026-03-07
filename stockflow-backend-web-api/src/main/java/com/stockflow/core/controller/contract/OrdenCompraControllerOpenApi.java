package com.stockflow.core.controller.contract;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.stockflow.core.dto.OrdenCompraDto;
import com.stockflow.core.utils.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RequestMapping("/api/ordenescompra")
public interface OrdenCompraControllerOpenApi {
     @Operation(summary = "Obtiene una orden de compra por su ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = OrdenCompraDto.class)))
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> findById(@PathVariable("id") Integer k);

     @Operation(summary = "Lista todos los ordenes de compra registradas")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = OrdenCompraDto.class)))
    @GetMapping(path = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> findAll();

    @Operation(summary = "Registra una nueva orden de compra")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Orden de compra registrada con éxito")
    @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> insert(@RequestBody OrdenCompraDto d);

    @Operation(summary = "Actualiza los datos de una orden de compra existente")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Orden de compra actualizada con éxito")
    @PutMapping(path = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> update(@RequestBody OrdenCompraDto d);

    @Operation(summary = "Eliminar una orden de compra")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Orden de compra eliminada con éxito")
    @DeleteMapping(path = "/delete/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> delete(@PathVariable("id") Integer k);

    @Operation(summary = "Genera un nuevo número de orden de compra")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = String.class)))
    @GetMapping(path = "/generate-number", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> generateNumeroOrden();

    @Operation(summary = "Lista las órdenes de compra pendientes de recepción")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = OrdenCompraDto.class)))
    @GetMapping(path = "/pendientes-recepcion", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse> findPendientesRecepcion();

}
