package com.stockflow.core.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    /* -------------------------------------------------------------------------- */
    /* Estados de movimiento de inventario */
    /* -------------------------------------------------------------------------- */
    ENTRADA("Entrada"),
    SALIDA("Salida"),
    TRANSFERENCIA("Transferencia Mercaderia"),
    AJUSTE_SALIDA_INVENTARIO("Ajustes Salida Inventario Mensual"),
    AJUSTE_ENTRADA_INVENTARIO("Ajustes Entrada Inventario Mensual"),
    ;

    private final String codigo;

    EnumCodigoEstado(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return this.codigo;
    }

    public List<String> getCodigos() {
        return Arrays.stream(values()).map(EnumCodigoEstado::getCodigo).toList();
    }

}
