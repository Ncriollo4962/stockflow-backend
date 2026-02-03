package com.stockflow.core.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.hateoas.RepresentationModel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.stockflow.core.entity.EntityDto;
import com.stockflow.core.entity.OrdenCompra;
import com.stockflow.core.entity.Proveedor;
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
public class OrdenCompraDto extends RepresentationModel<OrdenCompraDto>
      implements EntityDto<OrdenCompra, OrdenCompraDto>, Serializable {

   private static final long serialVersionUID = 1L;

   private Integer id;
   private String numeroOrden;
   private ProveedorDto proveedor;
   private UsuarioDto usuario;
   private LocalDateTime fechaCompra;
   private LocalDateTime fechaEntrega;
   @JsonProperty(access = JsonProperty.Access.READ_ONLY)
   private LocalDateTime fechaCreacion;
   private String estado;
   private BigDecimal totalCompra;
   private String notas;
   private Integer version;

   // --- Banderas de control (Template) ---
   @JsonIgnore
   @Builder.Default
   private boolean defId = true;
   @JsonIgnore
   @Builder.Default
   private boolean defNumeroOrden = true;
   @JsonIgnore
   @Builder.Default
   private boolean defFechaCompra = true;
   @JsonIgnore
   @Builder.Default
   private boolean defFechaEntrega = true;
   @JsonIgnore
   @Builder.Default
   private boolean defEstado = true;
   @JsonIgnore
   @Builder.Default
   private boolean defTotalCompra = true;
   @JsonIgnore
   @Builder.Default
   private boolean defNotas = true;
   @JsonIgnore
   @Builder.Default
   private boolean defVersion = true;
   @JsonIgnore
   @Builder.Default
   private boolean defFechaCreacion = true;
   @JsonIgnore
   @Builder.Default
   private ProveedorDto defProveedor = ProveedorDto.build();
   @JsonIgnore
   @Builder.Default
   private UsuarioDto defUsuario = UsuarioDto.build();

   public static OrdenCompraDto build() {
      return OrdenCompraDto.builder().build();
   }

   @Override
   public OrdenCompraDto fromEntity(OrdenCompra entity) {
      return fromEntity(this, entity);
   }

   @Override
   public OrdenCompraDto fromEntity(OrdenCompraDto template, OrdenCompra entity) {
      if (entity == null)
         return null;
      OrdenCompraDto dto = OrdenCompraDto.builder().build();

      if (template.isDefId())
         dto.setId(entity.getId());
      if (template.isDefNumeroOrden())
         dto.setNumeroOrden(entity.getNumeroOrden());
      if (template.isDefFechaCompra())
         dto.setFechaCompra(entity.getFechaCompra());
      if (template.isDefFechaEntrega())
         dto.setFechaEntrega(entity.getFechaEntrega());
      if (template.isDefEstado())
         dto.setEstado(entity.getEstado());
      if (template.isDefTotalCompra())
         dto.setTotalCompra(entity.getTotalCompra());
      if (template.isDefNotas())
         dto.setNotas(entity.getNotas());
      if (template.isDefVersion())
         dto.setVersion(entity.getVersion());
      if (template.isDefFechaCreacion())
         dto.setFechaCreacion(entity.getFechaCreacion());

      if (template.getDefProveedor() != null && entity.getProveedor() != null) {
         dto.setProveedor(ProveedorDto.build().fromEntity(template.getDefProveedor(), entity.getProveedor()));
      }
      if (template.getDefUsuario() != null && entity.getUsuario() != null) {
         dto.setUsuario(UsuarioDto.build().fromEntity(template.getDefUsuario(), entity.getUsuario()));
      }

      return dto;
   }

   @Override
   public OrdenCompra toEntity() {
      return OrdenCompra.builder()
            .id(this.id)
            .numeroOrden(this.numeroOrden)
            .fechaCompra(this.fechaCompra)
            .fechaEntrega(this.fechaEntrega)
            .estado(this.estado)
            .totalCompra(this.totalCompra)
            .notas(this.notas)
            .version(this.version)
            .proveedor(this.proveedor != null && this.proveedor.getId() != null
                  ? Proveedor.builder().id(this.proveedor.getId()).build()
                  : null)
            .usuario(this.usuario != null && this.usuario.getId() != null
                  ? Usuario.builder().id(this.usuario.getId()).build()
                  : null)
            .build();
   }
}
