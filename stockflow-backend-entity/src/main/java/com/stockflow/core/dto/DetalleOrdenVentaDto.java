package com.stockflow.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.stockflow.core.entity.DetalleOrdenVenta;
import com.stockflow.core.entity.EntityDto;
import com.stockflow.core.entity.OrdenVenta;
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
public class DetalleOrdenVentaDto extends RepresentationModel<DetalleOrdenVentaDto>
      implements EntityDto<DetalleOrdenVenta, DetalleOrdenVentaDto>, Serializable {

   private static final long serialVersionUID = 1L;

   private Integer id;
   private OrdenVentaDto ordenVenta;
   private ProductoDto producto;
   private Integer cantidad;
   private BigDecimal precioUnitario;
   @JsonProperty(access = JsonProperty.Access.READ_ONLY)
   private BigDecimal subtotal;

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
   private OrdenVentaDto defOrdenVenta = OrdenVentaDto.build();
   @JsonIgnore
   @Builder.Default
   private ProductoDto defProducto = ProductoDto.build();

   public static DetalleOrdenVentaDto build() {
      return DetalleOrdenVentaDto.builder().build();
   }

   @Override
   public DetalleOrdenVentaDto fromEntity(DetalleOrdenVenta entity) {
      return fromEntity(this, entity);
   }

   @Override
   public DetalleOrdenVentaDto fromEntity(DetalleOrdenVentaDto template, DetalleOrdenVenta entity) {
      if (entity == null)
         return null;
      DetalleOrdenVentaDto dto = DetalleOrdenVentaDto.builder().build();

      if (template.isDefId())
         dto.setId(entity.getId());
      if (template.isDefCantidad())
         dto.setCantidad(entity.getCantidad());
      if (template.isDefPrecioUnitario())
         dto.setPrecioUnitario(entity.getPrecioUnitario());
      if (template.isDefSubtotal())
         dto.setSubtotal(entity.getSubtotal());

      if (template.getDefOrdenVenta() != null && entity.getOrdenVenta() != null) {
         dto.setOrdenVenta(OrdenVentaDto.build().fromEntity(template.getDefOrdenVenta(), entity.getOrdenVenta()));
      }
      if (template.getDefProducto() != null && entity.getProducto() != null) {
         dto.setProducto(ProductoDto.build().fromEntity(template.getDefProducto(), entity.getProducto()));
      }

      return dto;
   }

   @Override
   public DetalleOrdenVenta toEntity() {
      return DetalleOrdenVenta.builder()
            .id(this.id)
            .cantidad(this.cantidad)
            .precioUnitario(this.precioUnitario)
            .ordenVenta(this.ordenVenta != null && this.ordenVenta.getId() != null
                  ? OrdenVenta.builder().id(this.ordenVenta.getId()).build()
                  : null)
            .producto(this.producto != null && this.producto.getId() != null
                  ? Producto.builder().id(this.producto.getId()).build()
                  : null)
            .build();
   }
}
