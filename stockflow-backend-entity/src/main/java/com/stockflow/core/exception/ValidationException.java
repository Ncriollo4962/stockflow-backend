package com.stockflow.core.exception;

import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {

    private String fieldName;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }
}
