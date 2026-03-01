package com.stockflow.core.enums;

public enum EnumCodigoEstado {
    PENDIENTE_DESPACHO,
    COMPLETADO,
    CANCELADO,
    PENDIENTE_PAGO,
    PENDIENTE_RECEPCION,
    ;

    public String getCodigo() {
        return this.name();
    }

}
