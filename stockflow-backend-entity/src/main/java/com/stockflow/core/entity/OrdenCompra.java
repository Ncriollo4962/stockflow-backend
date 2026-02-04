package com.stockflow.core.entity;

import java.math.BigDecimal;
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
@Table(name = "orden_compra")
@DynamicUpdate
public class OrdenCompra {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "numero_orden", unique = true, nullable = false, length = 50)
    private String numeroOrden;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    @ToString.Exclude
    private Proveedor proveedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    private Usuario usuario;

    @Column(name = "fecha_orden_compra", nullable = false)
    private LocalDateTime fechaCompra;

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;

    @Column(nullable = false, length = 50)
    private String estado;

    @Column(name = "total_compra", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCompra = BigDecimal.ZERO;

    @Column(length = 350)
    private String notas;

    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Version
    private Integer version;

    @OneToMany(mappedBy = "ordenCompra", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<DetalleOrdenCompra> detalleOrdenCompras;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrdenCompra other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
