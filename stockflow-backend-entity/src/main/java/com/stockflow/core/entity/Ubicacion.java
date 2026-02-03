package com.stockflow.core.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ubicacion")
public class Ubicacion {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false, length = 50)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private Boolean estado = true;

    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", insertable = false, updatable = false)
    private LocalDateTime fechaActualizacion;

    @Version
    private Integer version;

    @ToString.Exclude // Evita bucles infinitos
    @Builder.Default // Evita NullPointerException al usar Builder
    @OneToMany(mappedBy = "ubicacion", fetch = FetchType.LAZY)
    private List<InventarioItem> inventarioItems = new java.util.ArrayList<>();

    @ToString.Exclude // Evita bucles infinitos
    @Builder.Default // Evita NullPointerException al usar Builder
    @OneToMany(mappedBy = "ubicacion", fetch = FetchType.LAZY)
    private List<MovimientoInventario> movimientoInventarios = new java.util.ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Ubicacion other))
            return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
