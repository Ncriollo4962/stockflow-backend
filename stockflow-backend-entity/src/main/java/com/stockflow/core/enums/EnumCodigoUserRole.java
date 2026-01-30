package com.stockflow.core.enums;

public enum EnumCodigoUserRole {
    ADMIN_TI("admin_ti"),
    GERENTE_ALMACEN("gerente_almacen"),
    ALMACENERO("almacenero"),
    VENDEDOR("vendedor"),
    ASISTENTE("asistente");

    private String codigo;

    EnumCodigoUserRole(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return this.codigo;
    }

}
