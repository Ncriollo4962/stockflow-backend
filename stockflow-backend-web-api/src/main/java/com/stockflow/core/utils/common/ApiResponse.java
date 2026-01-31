package com.stockflow.core.utils.common;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse {

    private Boolean error;
    private String codigo;
    private String titulo;
    private String mensaje;
    private TypeException type;

    @Builder.Default
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fecha = LocalDateTime.now();

    private Object data;

    public static ApiResponse ok(String mensaje, Object data) {
        return ok("", mensaje, data);
    }

    public static ApiResponse ok(String titulo, String mensaje, Object data) {
        return load(false, titulo, mensaje, "200", data, null);
    }

    public static ApiResponse create(String mensaje, Object data) {
        return create("", mensaje, data);
    }

    public static ApiResponse create(String titulo, String mensaje, Object data) {
        return load(false, titulo, mensaje, "201", data, null);
    }

    public static ApiResponse error(String mensaje) {
        return load(true, "Error", mensaje, "500", null, TypeException.E);
    }

    private static ApiResponse load(boolean isError, String titulo, String mensaje, String codigo, Object data, TypeException type) {
        return ApiResponse.builder()
                .error(isError)
                .titulo(titulo)
                .mensaje(mensaje)
                .codigo(codigo)
                .data(data)
                .type(type)
                .build();
    }
}
