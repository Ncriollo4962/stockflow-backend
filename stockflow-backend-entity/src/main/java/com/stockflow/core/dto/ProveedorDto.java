package com.stockflow.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.stockflow.core.entity.EntityDto;
import com.stockflow.core.entity.Proveedor;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = false)
public class ProveedorDto extends RepresentationModel<ProveedorDto>
        implements EntityDto<Proveedor, ProveedorDto>, Serializable {

    private Integer id;
    private String codigo;
    private String nombre;
    private String contacto;
    private String email;
    private String telefono;
    private String direccion;
    private String ciudadPais;
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
    private boolean defContacto = true;
    @JsonIgnore
    @Builder.Default
    private boolean defEmail = true;
    @JsonIgnore
    @Builder.Default
    private boolean defTelefono = true;
    @JsonIgnore
    @Builder.Default
    private boolean defDireccion = true;
    @JsonIgnore
    @Builder.Default
    private boolean defciudadPais = true;
    @JsonIgnore
    @Builder.Default
    private boolean defEstado = true;
    @JsonIgnore
    @Builder.Default
    private boolean defVersion = true;

    public static ProveedorDto build() {
        return ProveedorDto.builder().build();
    }

    @Override
    public ProveedorDto fromEntity(Proveedor entity) {
        return fromEntity(this, entity);
    }

    @Override
    public ProveedorDto fromEntity(ProveedorDto template, Proveedor entity) {
        if (entity == null)
            return null;
        ProveedorDto dto = new ProveedorDto();
        if (template.isDefId())
            dto.setId(entity.getId());
        if (template.isDefCodigo())
            dto.setCodigo(entity.getCodigo());
        if (template.isDefNombre())
            dto.setNombre(entity.getNombre());
        if (template.isDefContacto())
            dto.setContacto(entity.getContacto());
        if (template.isDefEmail())
            dto.setEmail(entity.getEmail());
        if (template.isDefTelefono())
            dto.setTelefono(entity.getTelefono());
        if (template.isDefDireccion())
            dto.setDireccion(entity.getDireccion());
        if (template.isDefciudadPais())
            dto.setCiudadPais(entity.getCiudadPais());
        if (template.isDefEstado())
            dto.setEstado(entity.getEstado());
        if (template.isDefVersion())
            dto.setVersion(entity.getVersion());
        return dto;
    }

    @Override
    public Proveedor toEntity() {
        return Proveedor.builder()
                .id(this.id).codigo(this.codigo).nombre(this.nombre)
                .contacto(this.contacto).email(this.email).telefono(this.telefono)
                .direccion(this.direccion).ciudadPais(this.ciudadPais).estado(this.estado)
                .version(this.version).build();
    }

}
