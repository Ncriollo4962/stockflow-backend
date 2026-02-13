package com.stockflow.core.handler;

import com.stockflow.core.utils.common.TypeException;

public class AuthCustomException extends BusinessException {
   public AuthCustomException(String mensaje) {
       super("401", "Error de Autenticación", mensaje, TypeException.E);
    }
}
