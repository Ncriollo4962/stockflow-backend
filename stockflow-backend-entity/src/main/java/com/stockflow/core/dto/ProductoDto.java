package com.stockflow.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.stockflow.core.entity.EntityDto;
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
public class ProductoDto extends RepresentationModel<ProductoDto> implements EntityDto<Producto, ProductoDto>, Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String codigo;
    private String nombre;
    private BigDecimal precioVenta;
    private CategoriaDto categoria;

    @JsonIgnore
    @Builder.Default
    private boolean defId = true;
    @JsonIgnore
    @Builder.Default
    private boolean defNombre = true;
    @JsonIgnore
    @Builder.Default
    private CategoriaDto defCategoria = null; // null = no traer relación

    public static ProductoDto build() {
        return ProductoDto.builder().build();
    }

    @Override
    public ProductoDto fromEntity(Producto entity) {
        return fromEntity(this, entity);
    }

    @Override
    public ProductoDto fromEntity(ProductoDto template, Producto entity) {
        if (entity == null) return null;
        ProductoDto dto = ProductoDto.builder().build();

        if (template.isDefId()) dto.setId(entity.getId());
        if (template.isDefNombre()) dto.setNombre(entity.getNombre());

        // Mapeo condicional de la relación
        if (template.getDefCategoria() != null && entity.getCategoria() != null) {
            dto.setCategoria(CategoriaDto.build().fromEntity(template.getDefCategoria(), entity.getCategoria()));
        }

        return dto;
    }

    @Override
    public Producto toEntity() {
        return Producto.builder()
                .id(this.id)
                .codigo(this.codigo)
                .nombre(this.nombre)
                .precioVenta(this.precioVenta)
                .build();
    }
}
