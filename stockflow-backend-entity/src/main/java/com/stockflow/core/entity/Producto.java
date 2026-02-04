package com.stockflow.core.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "producto")
@DynamicUpdate
public class Producto {

   private static final long serialVersionUID = 1L;

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Integer id;

   @Column(unique = true, nullable = false, length = 50)
   private String codigo;

   @Column(nullable = false, length = 150)
   private String nombre;

   @Column(length = 350)
    private String descripcion;

   // Relación Many-to-One con Categoría
   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "categoria_id", nullable = false)
   @ToString.Exclude
   private Categoria categoria;

   @Column(name = "precio_costo", nullable = false, precision = 10, scale = 2)
   private BigDecimal precioCosto;

   @Column(name = "precio_venta", nullable = false, precision = 10, scale = 2)
   private BigDecimal precioVenta;

   @Column(name = "cantidad_minima", nullable = false)
   private Integer cantidadMinima = 0;

   private Boolean estado = true;

   @Column(name = "fecha_creacion", insertable = false, updatable = false)
   private LocalDateTime fechaCreacion;

   @Column(name = "fecha_actualizacion", insertable = false, updatable = false)
   private LocalDateTime fechaActualizacion;

   @Version // El escudo contra errores de stock
   private Integer version;

   @ToString.Exclude // Evita bucles infinitos
   @Builder.Default // Evita NullPointerException al usar Builder
   @OneToMany(mappedBy = "producto", fetch = FetchType.LAZY)
   private List<InventarioItem> inventarioItems = new ArrayList<>();

   @ToString.Exclude // Evita bucles infinitos
   @Builder.Default // Evita NullPointerException al usar Builder
   @OneToMany(mappedBy = "producto", fetch = FetchType.LAZY)
   private List<MovimientoInventario> movimientoInventarios = new ArrayList<>();

   @ToString.Exclude // Evita bucles infinitos
   @Builder.Default // Evita NullPointerException al usar Builder
   @OneToMany(mappedBy = "producto", fetch = FetchType.LAZY)
   private List<DetalleOrdenCompra> detalleOrdenCompras = new ArrayList<>();

   @ToString.Exclude // Evita bucles infinitos
   @Builder.Default // Evita NullPointerException al usar Builder
   @OneToMany(mappedBy = "producto", fetch = FetchType.LAZY)
   private List<DetalleOrdenVenta> detalleOrdenVenta = new ArrayList<>();

   @Override
   public boolean equals(Object o) {
      if (this == o)
         return true;
      if (!(o instanceof Producto other))
         return false;
      return id != null && id.equals(other.getId());
   }

   @Override
   public int hashCode() {
      return getClass().hashCode();
   }

}
