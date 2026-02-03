package com.stockflow.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "detalle_orden_venta")
@DynamicUpdate
public class DetalleOrdenVenta {

   private static final long serialVersionUID = 1L;

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Integer id;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "orden_venta_id", nullable = false)
   @ToString.Exclude
   private OrdenVenta ordenVenta;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "producto_id", nullable = false)
   @ToString.Exclude
   private Producto producto;

   @Column(nullable = false)
   private Integer cantidad;

   @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
   private BigDecimal precioUnitario;

   @Column(insertable = false, updatable = false, precision = 12, scale = 2)
   private BigDecimal subtotal;

   @Override
   public boolean equals(Object o) {
      if (this == o)
         return true;
      if (!(o instanceof DetalleOrdenVenta other))
         return false;
      return id != null && id.equals(other.getId());
   }

   @Override
   public int hashCode() {
      return getClass().hashCode();
   }
}
