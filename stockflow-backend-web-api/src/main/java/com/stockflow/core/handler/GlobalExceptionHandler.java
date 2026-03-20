package com.stockflow.core.handler;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.stockflow.core.exception.ConflictException;
import com.stockflow.core.utils.common.ApiResponse;
import com.stockflow.core.utils.common.TypeException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Manejo de Integridad Referencial y Restricciones Únicas
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        String mensaje = "Error de integridad de datos.";
        String titulo = "Conflicto de Integridad";
        
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("Duplicate entry")) {
                mensaje = "Ya existe un registro con los mismos datos únicos (ej. código, número, email).";
                titulo = "Dato Duplicado";
            } else if (ex.getMessage().contains("foreign key constraint fails")) {
                mensaje = "No se puede eliminar o modificar el registro porque tiene datos asociados.";
                titulo = "Restricción Referencial";
            }
        }

        ApiResponse response = ApiResponse.builder()
                .error(true)
                .codigo("409")
                .titulo(titulo)
                .mensaje(mensaje)
                .type(TypeException.E)
                .data(ex.getMostSpecificCause().getMessage()) // Agregamos detalles técnicos en data para debug
                .build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // 2. Manejo de Excepciones Generales (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGeneralException(Exception ex) {
        ApiResponse response = ApiResponse.builder()
                .error(true)
                .codigo("500")
                .titulo("Error Interno")
                .mensaje("Ha ocurrido un error inesperado: " + ex.getMessage())
                .type(TypeException.E)
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    // 3. Manejo de Conflicto de Versión (Optimistic Locking)
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse> handleConflict(ConflictException ex) {
        ApiResponse response = ApiResponse.builder()
                .error(true)
                .codigo("409")
                .titulo("Conflicto de Versión")
                .mensaje(ex.getMessage())
                .type(TypeException.W)
                .data(ex.getCurrentData()) 
                .build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse> handleBusinessException(BusinessException ex) {
        ApiResponse response = ApiResponse.builder()
                .error(true)
                .codigo(ex.getCodigo())
                .titulo(ex.getTitulo())
                .mensaje(ex.getMessage())
                .type(ex.getType())
                .build();
        return new ResponseEntity<>(response, HttpStatus.valueOf(Integer.parseInt(ex.getCodigo())));

    }

}
