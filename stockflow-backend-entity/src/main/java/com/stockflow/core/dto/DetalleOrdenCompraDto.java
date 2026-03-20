package com.stockflow.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.stockflow.core.entity.DetalleOrdenCompra;
import com.stockflow.core.entity.EntityDto;
import com.stockflow.core.entity.OrdenCompra;
import com.stockflow.core.entity.Producto;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = false)
public class DetalleOrdenCompraDto extends RepresentationModel<DetalleOrdenCompraDto>
      implements EntityDto<DetalleOrdenCompra, DetalleOrdenCompraDto>, Serializable {

   private static final long serialVersionUID = 1L;

   private Integer id;
   private OrdenCompraDto ordenCompra;
   private ProductoDto producto;
   private Integer cantidad;
   private BigDecimal precioUnitario;
   @JsonProperty(access = JsonProperty.Access.READ_ONLY)
   private BigDecimal subtotal;
   private Integer cantidadRecibida;
   @JsonProperty(access = JsonProperty.Access.READ_ONLY)
   private Integer cantidadPendiente;
   private String estadoDetalle;

   // --- Banderas de control (Template) ---
   @JsonIgnore
   @Builder.Default
   private boolean defId = true;
   @JsonIgnore
   @Builder.Default
   private boolean defCantidad = true;
   @JsonIgnore
   @Builder.Default
   private boolean defPrecioUnitario = true;
   @JsonIgnore
   @Builder.Default
   private boolean defSubtotal = true;
   @JsonIgnore
   @Builder.Default
   private boolean defCantidadRecibida = true;
   @JsonIgnore
   @Builder.Default
   private boolean defCantidadPendiente = true;
   @JsonIgnore
   @Builder.Default
   private boolean defEstadoDetalle = true;

   @JsonIgnore
   @Builder.Default
   private OrdenCompraDto defOrdenCompra = OrdenCompraDto.build();
   @JsonIgnore
   @Builder.Default
   private ProductoDto defProducto = ProductoDto.build();

   public static DetalleOrdenCompraDto build() {
      return DetalleOrdenCompraDto.builder().build();
   }

   @Override
   public DetalleOrdenCompraDto fromEntity(DetalleOrdenCompra entity) {
      return fromEntity(this, entity);
   }

   @Override
   public DetalleOrdenCompraDto fromEntity(DetalleOrdenCompraDto template, DetalleOrdenCompra entity) {
      if (entity == null)
         return null;
      DetalleOrdenCompraDto dto = DetalleOrdenCompraDto.builder().build();

      if (template.isDefId())
         dto.setId(entity.getId());
      if (template.isDefCantidad())
         dto.setCantidad(entity.getCantidad());
      if (template.isDefPrecioUnitario())
         dto.setPrecioUnitario(entity.getPrecioUnitario());
      if (template.isDefSubtotal())
         dto.setSubtotal(entity.getSubtotal());
      if (template.isDefCantidadRecibida())
         dto.setCantidadRecibida(entity.getCantidadRecibida());
      if (template.isDefCantidadPendiente())
         dto.setCantidadPendiente(entity.getCantidadPendiente());
      if (template.isDefEstadoDetalle())
         dto.setEstadoDetalle(entity.getEstadoDetalle());

      if (template.getDefOrdenCompra() != null && entity.getOrdenCompra() != null) {
         // Evitar recursión infinita: La orden dentro del detalle NO debe traer sus detalles de vuelta
         OrdenCompraDto ordenTemplate = template.getDefOrdenCompra();
         ordenTemplate.setDefDetallesOrdenCompra(null);
         
         dto.setOrdenCompra(ordenTemplate.fromEntity(entity.getOrdenCompra()));
      }
      if (template.getDefProducto() != null && entity.getProducto() != null) {
         dto.setProducto(ProductoDto.build().fromEntity(template.getDefProducto(), entity.getProducto()));
      }

      return dto;
   }

   @Override
   public DetalleOrdenCompra toEntity() {
      return DetalleOrdenCompra.builder()
            .id(this.id)
            .cantidad(this.cantidad)
            .precioUnitario(this.precioUnitario)
            .cantidadRecibida(this.cantidadRecibida != null ? this.cantidadRecibida : 0)
            .estadoDetalle(this.estadoDetalle != null ? this.estadoDetalle : "Pendiente Recepción")
            .ordenCompra(this.ordenCompra != null && this.ordenCompra.getId() != null
                  ? OrdenCompra.builder()
                  .id(this.ordenCompra.getId())
                  .version(this.ordenCompra.getVersion()).build()
                  : null)
            .producto(this.producto != null && this.producto.getId() != null
                  ? Producto.builder()
                  .id(this.producto.getId())
                  .version(this.producto.getVersion()).build()
                  : null)
            .build();
   }
}
