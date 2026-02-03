package com.stockflow.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orden_venta")
@DynamicUpdate
public class OrdenVenta {

   private static final long serialVersionUID = 1L;

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Integer id;

   @Column(name = "numero_orden", unique = true, nullable = false, length = 50)
   private String numeroOrden;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "usuario_id", nullable = false)
   @ToString.Exclude
   private Usuario usuario;

   @Column(name = "cliente_nombre", length = 150)
   private String clienteNombre;

   @Column(name = "cliente_email", length = 100)
   private String clienteEmail;

   @Column(name = "cliente_telefono", length = 20)
   private String clienteTelefono;

   @Column(length = 200)
   private String direccion;

   @Column(name = "fecha_orden", nullable = false)
   private LocalDateTime fechaVenta;

   @Column(name = "total_orden", updatable = false, nullable = false, precision = 12, scale = 2)
   private BigDecimal totalVenta = BigDecimal.ZERO;

   @Column(nullable = false, length = 50)
   private String estado;

   @Version
   private Integer version;

   @ToString.Exclude // Evita bucles infinitos
   @Builder.Default // Evita NullPointerException al usar Builder
   @OneToMany(mappedBy = "ordenVenta", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
   private List<DetalleOrdenVenta> detalleOrdenVenta = new ArrayList<>();

   @Override
   public boolean equals(Object o) {
      if (this == o)
         return true;
      if (!(o instanceof OrdenVenta other))
         return false;
      return id != null && id.equals(other.getId());
   }

   @Override
   public int hashCode() {
      return getClass().hashCode();
   }
}
