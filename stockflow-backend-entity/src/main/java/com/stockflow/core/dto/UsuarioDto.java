package com.stockflow.core.dto;

import java.io.Serializable;

import org.springframework.hateoas.RepresentationModel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.stockflow.core.entity.EntityDto;
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
public class UsuarioDto extends RepresentationModel<UsuarioDto> implements EntityDto<Usuario, UsuarioDto>, Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String codigo;
    private String nombre;
    private String email;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String contrasena;
    private String rol;
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
    private boolean defEmail = true;
    @JsonIgnore
    @Builder.Default
    private boolean defRol = true;
    @JsonIgnore
    @Builder.Default
    private boolean defEstado = true;
    @JsonIgnore
    @Builder.Default
    private boolean defVersion = true;

    public static UsuarioDto build() {
        return UsuarioDto.builder().build();
    }

    @Override
    public UsuarioDto fromEntity(Usuario entity) {
        return fromEntity(this, entity);
    }

    @Override
    public UsuarioDto fromEntity(UsuarioDto template, Usuario entity) {
        if (entity == null) return null;

        UsuarioDto dto = UsuarioDto.builder().build();

        if (template.isDefId()) dto.setId(entity.getId());
        if (template.isDefCodigo()) dto.setCodigo(entity.getCodigo());
        if (template.isDefNombre()) dto.setNombre(entity.getNombre());
        if (template.isDefEmail()) dto.setEmail(entity.getEmail());
        if (template.isDefRol()) dto.setRol(entity.getRol());
        if (template.isDefEstado()) dto.setEstado(entity.getEstado());
        if (template.isDefVersion()) dto.setVersion(entity.getVersion());

        return dto;

    }

    @Override
    public Usuario toEntity() {
        return Usuario.builder()
                .id(this.id)
                .codigo(this.codigo)
                .nombre(this.nombre)
                .email(this.email)
                .contrasena(this.contrasena)
                .rol(this.rol)
                .estado(this.estado)
                .version(this.version)
                .build();
    }
}
