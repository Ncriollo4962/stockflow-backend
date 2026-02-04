package com.stockflow.core.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "proveedor")
@DynamicUpdate
public class Proveedor {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false, length = 50)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 100)
    private String contacto;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String telefono;

    @Column(length = 350)
    private String direccion;

    @Column(name = "ciudad_pais", length = 100)
    private String ciudadPais;

    @Column(nullable = false)
    private Boolean estado = true;

    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @ToString.Exclude // Evita bucles infinitos
    @Builder.Default // Evita NullPointerException al usar Builder
    @OneToMany(mappedBy = "proveedor", fetch = FetchType.LAZY)
    private List<OrdenCompra> ordenCompras = new java.util.ArrayList<>();

    @Version
    private Integer version;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Proveedor other))
            return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
