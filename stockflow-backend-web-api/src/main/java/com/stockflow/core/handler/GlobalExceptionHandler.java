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

    // 1. Manejo de Integridad Referencial (El error 1451 de MySQL)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        ApiResponse response = ApiResponse.builder()
                .error(true)
                .codigo("409")
                .titulo("Conflicto de Integridad")
                .mensaje("No se puede eliminar el registro porque tiene datos asociados.")
                .type(TypeException.E)
                .data("")
                .build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
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
                .data(ex.getCurrentData()) // Enviamos los datos reales de la BD
                .build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

}
