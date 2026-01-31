package com.stockflow.core.utils;

import com.stockflow.core.exception.ValidationException;

public class ValidationUtil {

    private ValidationUtil() {}

    /**
     * Valida si un valor es nulo o vacío y lanza RuntimeException con mensaje personalizado.
     */
    public static void isRequired(Object value, String message) {
        if (value == null || (value instanceof String s && s.trim().isEmpty())) {
            throw new ValidationException(message);
        }
    }
}
