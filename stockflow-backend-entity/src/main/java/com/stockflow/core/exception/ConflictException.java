package com.stockflow.core.exception;


import lombok.Getter;

@Getter
public class ConflictException extends RuntimeException {
    private final Object currentData;

    public ConflictException(String message, Object currentData) {
        super(message);
        this.currentData = currentData;
    }
}
