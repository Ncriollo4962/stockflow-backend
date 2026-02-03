package com.stockflow.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.stockflow.core.entity.EntityDto;
import com.stockflow.core.entity.OrdenVenta;
import com.stockflow.core.entity.Usuario;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = false)
public class OrdenVentaDto extends RepresentationModel<OrdenVentaDto>
      implements EntityDto<OrdenVenta, OrdenVentaDto>, Serializable {

   private static final long serialVersionUID = 1L;

   private Integer id;
   private String numeroOrden;
   private UsuarioDto usuario;
   private String clienteNombre;
   private String clienteEmail;
   private String clienteTelefono;
   private String direccion;
   private LocalDateTime fechaVenta;
   @JsonProperty(access = JsonProperty.Access.READ_ONLY)
   private BigDecimal totalVenta;
   private String estado;
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
   private boolean defClienteNombre = true;
   @JsonIgnore
   @Builder.Default
   private boolean defClienteEmail = true;
   @JsonIgnore
   @Builder.Default
   private boolean defClienteTelefono = true;
   @JsonIgnore
   @Builder.Default
   private boolean defDireccion = true;
   @JsonIgnore
   @Builder.Default
   private boolean defFechaVenta = true;
   @JsonIgnore
   @Builder.Default
   private boolean defTotalVenta = true;
   @JsonIgnore
   @Builder.Default
   private boolean defEstado = true;
   @JsonIgnore
   @Builder.Default
   private boolean defVersion = true;

   @JsonIgnore
   @Builder.Default
   private UsuarioDto defUsuario = UsuarioDto.build();

   public static OrdenVentaDto build() {
      return OrdenVentaDto.builder().build();
   }

   @Override
   public OrdenVentaDto fromEntity(OrdenVenta entity) {
      return fromEntity(this, entity);
   }

   @Override
   public OrdenVentaDto fromEntity(OrdenVentaDto template, OrdenVenta entity) {
      if (entity == null)
         return null;
      OrdenVentaDto dto = OrdenVentaDto.builder().build();

      if (template.isDefId())
         dto.setId(entity.getId());
      if (template.isDefNumeroOrden())
         dto.setNumeroOrden(entity.getNumeroOrden());
      if (template.isDefClienteNombre())
         dto.setClienteNombre(entity.getClienteNombre());
      if (template.isDefClienteEmail())
         dto.setClienteEmail(entity.getClienteEmail());
      if (template.isDefClienteTelefono())
         dto.setClienteTelefono(entity.getClienteTelefono());
      if (template.isDefDireccion())
         dto.setDireccion(entity.getDireccion());
      if (template.isDefFechaVenta())
         dto.setFechaVenta(entity.getFechaVenta());
      if (template.isDefTotalVenta())
         dto.setTotalVenta(entity.getTotalVenta());
      if (template.isDefEstado())
         dto.setEstado(entity.getEstado());
      if (template.isDefVersion())
         dto.setVersion(entity.getVersion());

      if (template.getDefUsuario() != null && entity.getUsuario() != null) {
         dto.setUsuario(UsuarioDto.build().fromEntity(template.getDefUsuario(), entity.getUsuario()));
      }

      return dto;
   }

   @Override
   public OrdenVenta toEntity() {
      return OrdenVenta.builder()
            .id(this.id)
            .numeroOrden(this.numeroOrden)
            .clienteNombre(this.clienteNombre)
            .clienteEmail(this.clienteEmail)
            .clienteTelefono(this.clienteTelefono)
            .direccion(this.direccion)
            .fechaVenta(this.fechaVenta)
            .estado(this.estado)
            .version(this.version)
            .usuario(this.usuario != null && this.usuario.getId() != null
                  ? Usuario.builder().id(this.usuario.getId()).build()
                  : null)
            .build();
   }
}
