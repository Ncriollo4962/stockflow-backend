package com.stockflow.core.enums;

public enum EnumCodigoEstado {
    /* -------------------------------------------------------------------------- */
    /* Estados de la orden de compra */
    /* -------------------------------------------------------------------------- */
    APERTURADA("Aperturada"),
    APROBADA("Aprobada"),
    ENVIADA("Enviada"),
    RECHAZADA("Rechazada"),
    ANULADA("Anulada"),
    PENDIENTE_RECEPCION("Pendiente Recepción"),
    RECIBIDA_PARCIAL("Recibida Parcial"),
    RECIBIDA_COMPLETA("Recibida Completa"),
    FINALIZADA("Finalizada"),
    /* -------------------------------------------------------------------------- */
    /* Estados de la orden de venta */
    /* -------------------------------------------------------------------------- */
    PENDIENTE_DESPACHO("Pendiente Despacho"),
    ;

    private final String codigo;

    EnumCodigoEstado(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return this.codigo;
    }

}
