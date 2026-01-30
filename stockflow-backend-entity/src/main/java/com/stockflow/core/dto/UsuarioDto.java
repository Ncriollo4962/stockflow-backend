package com.stockflow.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.stockflow.core.entity.EntityDto;
import com.stockflow.core.entity.Usuario;
import com.stockflow.core.enums.EnumCodigoUserRole;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = false)
public class UsuarioDto  extends RepresentationModel<UsuarioDto> implements EntityDto<Usuario, UsuarioDto>, Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String codigo;
    private String nombre;
    private String email;
    private EnumCodigoUserRole rol;
    private Boolean estado;

    // Banderas de control (Template) para decidir qué campos poblar
    @JsonIgnore @Builder.Default private boolean defId = true;
    @JsonIgnore @Builder.Default private boolean defCodigo = true;
    @JsonIgnore @Builder.Default private boolean defNombre = true;
    @JsonIgnore @Builder.Default private boolean defEmail = true;
    @JsonIgnore @Builder.Default private boolean defRol = true;
    @JsonIgnore @Builder.Default private boolean defEstado = true;

    public static UsuarioDto build() {
        return UsuarioDto.builder().build();
    }

    @Override
    public UsuarioDto fromEntity(Usuario entity) {
        return fromEntity(this, entity);
    }

    @Override
    public UsuarioDto fromEntity(UsuarioDto template, Usuario entity) {
        if (entity != null) {
            UsuarioDto dto = UsuarioDto.builder().build();
            if (template.isDefId()) dto.setId(entity.getId());
            if (template.isDefCodigo()) dto.setCodigo(entity.getCodigo());
            if (template.isDefNombre()) dto.setNombre(entity.getNombre());
            if (template.isDefEmail()) dto.setEmail(entity.getEmail());
            if (template.isDefRol()) dto.setRol(entity.getRol());
            if (template.isDefEstado()) dto.setEstado(entity.getEstado());
            return dto;
        }
        return null;
    }

    @Override
    public Usuario toEntity() {
        return Usuario.builder()
                .id(this.id)
                .codigo(this.codigo)
                .nombre(this.nombre)
                .email(this.email)
                .rol(this.rol)
                .estado(this.estado)
                .build();
    }
}
