package com.stockflow.core.handler;

import com.stockflow.core.utils.common.TypeException;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final String codigo;
    private final String titulo;
    private final TypeException type;

    public BusinessException(String codigo, String titulo, String mensaje, TypeException type) {
        super(mensaje);
        this.codigo = codigo;
        this.titulo = titulo;
        this.type = type;
    }

}
