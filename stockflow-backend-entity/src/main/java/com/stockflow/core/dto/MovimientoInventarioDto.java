package com.stockflow.core.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.hateoas.RepresentationModel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.stockflow.core.entity.EntityDto;
import com.stockflow.core.entity.MovimientoInventario;
import com.stockflow.core.entity.Producto;
import com.stockflow.core.entity.Ubicacion;
import com.stockflow.core.entity.Usuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = false)
public class MovimientoInventarioDto extends RepresentationModel<MovimientoInventarioDto>
      implements EntityDto<MovimientoInventario, MovimientoInventarioDto>, Serializable {

   private static final long serialVersionUID = 1L;

   private Integer id;
   private ProductoDto producto;
   private UbicacionDto ubicacion;
   private String tipoMovimiento;
   private Integer cantidad;
   private String motivo;
   private UsuarioDto usuario;
   private String referencia;
   private LocalDateTime fechaMovimiento;
   private String notas;
   private Integer version;

   // --- Banderas de control (Template) ---
   @JsonIgnore
   @Builder.Default
   private boolean defId = true;
   @JsonIgnore
   @Builder.Default
   private boolean defTipoMovimiento = true;
   @JsonIgnore
   @Builder.Default
   private boolean defCantidad = true;
   @JsonIgnore
   @Builder.Default
   private boolean defMotivo = true;
   @JsonIgnore
   @Builder.Default
   private boolean defReferencia = true;
   @JsonIgnore
   @Builder.Default
   private boolean defFechaMovimiento = true;
   @JsonIgnore
   @Builder.Default
   private boolean defNotas = true;
   @JsonIgnore
   @Builder.Default
   private boolean defVersion = true;

   @JsonIgnore
   @Builder.Default
   private ProductoDto defProducto = ProductoDto.build();
   @JsonIgnore
   @Builder.Default
   private UbicacionDto defUbicacion = UbicacionDto.build();
   @JsonIgnore
   @Builder.Default
   private UsuarioDto defUsuario = UsuarioDto.build();

   public static MovimientoInventarioDto build() {
      return MovimientoInventarioDto.builder().build();
   }

   @Override
   public MovimientoInventarioDto fromEntity(MovimientoInventario entity) {
      return fromEntity(this, entity);
   }

   @Override
   public MovimientoInventarioDto fromEntity(MovimientoInventarioDto template, MovimientoInventario entity) {
      if (entity == null)
         return null;
      MovimientoInventarioDto dto = MovimientoInventarioDto.builder().build();

      if (template.isDefId())
         dto.setId(entity.getId());
      if (template.isDefTipoMovimiento())
         dto.setTipoMovimiento(entity.getTipoMovimiento());
      if (template.isDefCantidad())
         dto.setCantidad(entity.getCantidad());
      if (template.isDefMotivo())
         dto.setMotivo(entity.getMotivo());
      if (template.isDefReferencia())
         dto.setReferencia(entity.getReferencia());
      if (template.isDefFechaMovimiento())
         dto.setFechaMovimiento(entity.getFechaMovimiento());
      if (template.isDefNotas())
         dto.setNotas(entity.getNotas());
      if (template.isDefVersion())
         dto.setVersion(entity.getVersion());

      if (template.getDefProducto() != null && entity.getProducto() != null) {
         dto.setProducto(ProductoDto.build().fromEntity(template.getDefProducto(), entity.getProducto()));
      }
      if (template.getDefUbicacion() != null && entity.getUbicacion() != null) {
         dto.setUbicacion(UbicacionDto.build().fromEntity(template.getDefUbicacion(), entity.getUbicacion()));
      }
      if (template.getDefUsuario() != null && entity.getUsuario() != null) {
         dto.setUsuario(UsuarioDto.build().fromEntity(template.getDefUsuario(), entity.getUsuario()));
      }

      return dto;
   }

   @Override
   public MovimientoInventario toEntity() {
      return MovimientoInventario.builder()
            .id(this.id)
            .tipoMovimiento(this.tipoMovimiento)
            .cantidad(this.cantidad)
            .motivo(this.motivo)
            .referencia(this.referencia)
            .fechaMovimiento(this.fechaMovimiento)
            .notas(this.notas)
            .version(this.version)
            .producto(this.producto != null && this.producto.getId() != null
                  ? Producto.builder().id(this.producto.getId()).build()
                  : null)
            .ubicacion(this.ubicacion != null && this.ubicacion.getId() != null
                  ? Ubicacion.builder().id(this.ubicacion.getId()).build()
                  : null)
            .usuario(this.usuario != null && this.usuario.getId() != null
                  ? Usuario.builder().id(this.usuario.getId()).build()
                  : null)
            .build();
   }
}
