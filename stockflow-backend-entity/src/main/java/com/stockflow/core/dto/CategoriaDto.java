package com.stockflow.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.stockflow.core.entity.Categoria;
import com.stockflow.core.entity.EntityDto;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = false)
public class CategoriaDto extends RepresentationModel<CategoriaDto> implements EntityDto<Categoria, CategoriaDto>, Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Boolean estado;
    private Integer version;

    // Banderas de control (Template) para decidir qué campos poblar
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

    public static CategoriaDto build() {
        return CategoriaDto.builder().build();
    }

    @Override
    public CategoriaDto fromEntity(Categoria entity) {
        return fromEntity(this, entity);
    }

    @Override
    public CategoriaDto fromEntity(CategoriaDto template, Categoria entity) {
        if (entity == null) return null;
        CategoriaDto dto = CategoriaDto.builder().build();

        if (template.isDefId()) dto.setId(entity.getId());
        if (template.isDefCodigo()) dto.setCodigo(entity.getCodigo());
        if (template.isDefNombre()) dto.setNombre(entity.getNombre());
        if (template.isDefDescripcion()) dto.setDescripcion(entity.getDescripcion());
        if (template.isDefEstado()) dto.setEstado(entity.getEstado());
        if (template.isDefVersion()) dto.setVersion(entity.getVersion());

        return dto;
    }

    @Override
    public Categoria toEntity() {
        return Categoria.builder()
                .id(this.id)
                .codigo(this.codigo)
                .nombre(this.nombre)
                .descripcion(this.descripcion)
                .estado(this.estado)
                .version(this.version)
                .build();
    }
}
