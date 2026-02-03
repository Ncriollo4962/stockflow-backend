package com.stockflow.core.dto;

import java.io.Serializable;

import org.springframework.hateoas.RepresentationModel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.stockflow.core.entity.EntityDto;
import com.stockflow.core.entity.Ubicacion;

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
public class UbicacionDto extends RepresentationModel<UbicacionDto>
      implements EntityDto<Ubicacion, UbicacionDto>, Serializable {

   private Integer id;
   private String codigo;
   private String nombre;
   private String descripcion;
   private Boolean estado;
   private Integer version;

   @JsonIgnore
   @Builder.Default
   private boolean defId = true;
   @JsonIgnore
   @Builder.Default
   private boolean defCodigo = true;
   @JsonIgnore
   @Builder.Default
   private boolean defNombre = true;
   @JsonIgnore
   @Builder.Default
   private boolean defDescripcion = true;
   @JsonIgnore
   @Builder.Default
   private boolean defEstado = true;
   @JsonIgnore
   @Builder.Default
   private boolean defVersion = true;

   public static UbicacionDto build() {
      return UbicacionDto.builder().build();
   }

   @Override
   public UbicacionDto fromEntity(Ubicacion entity) {
      return fromEntity(this, entity);
   }

   @Override
   public UbicacionDto fromEntity(UbicacionDto template, Ubicacion entity) {
      if (entity == null) return null;
      UbicacionDto dto = new UbicacionDto();
      if (template.isDefId())
         dto.setId(entity.getId());
      if (template.isDefCodigo())
         dto.setCodigo(entity.getCodigo());
      if (template.isDefNombre())
         dto.setNombre(entity.getNombre());
      if (template.isDefDescripcion())
         dto.setDescripcion(entity.getDescripcion());
      if (template.isDefEstado())
         dto.setEstado(entity.getEstado());
      if (template.isDefVersion())
         dto.setVersion(entity.getVersion());
      return dto;
   }

   @Override
   public Ubicacion toEntity() {
      return Ubicacion.builder()
            .id(this.id)
            .codigo(this.codigo)
            .nombre(this.nombre)
            .descripcion(this.descripcion)
            .estado(this.estado)
            .version(this.version)
            .build();
   }

}
