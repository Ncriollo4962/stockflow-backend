package com.stockflow.core.enums;

public enum EnumCodigoUserRole {
    ROLE_ADMIN_TI,
    ROLE_GERENTE_ALMACEN,
    ROLE_ALMACENERO,
    ROLE_VENDEDOR,
    ROLE_ASISTENTE;

    public String getCodigo() {
        return this.name();
    }

}
