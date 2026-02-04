package com.stockflow.core.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "movimiento_inventario")
@DynamicUpdate
public class MovimientoInventario {

   private static final long serialVersionUID = 1L;

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Integer id;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "producto_id", nullable = false)
   @ToString.Exclude
   private Producto producto;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "ubicacion_id", nullable = false)
   @ToString.Exclude
   private Ubicacion ubicacion;

   @Column(name = "tipo_movimiento", nullable = false, length = 50)
   private String tipoMovimiento;

   @Column(nullable = false)
   private Integer cantidad;

   @Column(length = 200)
   private String motivo;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "usuario_id", nullable = false)
   @ToString.Exclude
   private Usuario usuario;

   @Column(length = 50)
   private String referencia;

   @Column(name = "fecha_movimiento", nullable = false)
   private LocalDateTime fechaMovimiento;

   @Column(length = 350)
    private String notas;

   @Version
   private Integer version;

   @Override
   public boolean equals(Object o) {
      if (this == o)
         return true;
      if (!(o instanceof MovimientoInventario other))
         return false;
      return id != null && id.equals(other.getId());
   }

   @Override
   public int hashCode() {
      return getClass().hashCode();
   }

}
