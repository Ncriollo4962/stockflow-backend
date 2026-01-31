package com.stockflow.core.handler;

import com.stockflow.core.exception.ConflictException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

public class GlobalExceptionHandler {

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<?> handleConflict(ConflictException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        body.put("currentData", ex.getCurrentData()); // Enviamos los datos reales de la BD

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

}
