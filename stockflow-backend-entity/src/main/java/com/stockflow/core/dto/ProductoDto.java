package com.stockflow.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.stockflow.core.entity.Categoria;
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
    private String descripcion;
    private BigDecimal precioCosto;
    private BigDecimal precioVenta;
    private Integer cantidadMinima;
    private Boolean estado;
    private Integer version;
    private CategoriaDto categoria;

    // --- Banderas de control (Template) ---
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
    private boolean defPrecioCosto = true;
    @JsonIgnore
    @Builder.Default
    private boolean defPrecioVenta = true;
    @JsonIgnore
    @Builder.Default
    private boolean defCantidadMinima = true;
    @JsonIgnore
    @Builder.Default
    private boolean defEstado = true;
    @JsonIgnore
    @Builder.Default
    private boolean defVersion = true;
    @JsonIgnore
    @Builder.Default
    private CategoriaDto defCategoria = CategoriaDto.build();

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
        if (template.isDefCodigo()) dto.setCodigo(entity.getCodigo());
        if (template.isDefNombre()) dto.setNombre(entity.getNombre());
        if (template.isDefDescripcion()) dto.setDescripcion(entity.getDescripcion());
        if (template.isDefPrecioCosto()) dto.setPrecioCosto(entity.getPrecioCosto());
        if (template.isDefPrecioVenta()) dto.setPrecioVenta(entity.getPrecioVenta());
        if (template.isDefCantidadMinima()) dto.setCantidadMinima(entity.getCantidadMinima());
        if (template.isDefEstado()) dto.setEstado(entity.getEstado());
        if (template.isDefVersion()) dto.setVersion(entity.getVersion());

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
                .descripcion(this.descripcion)
                .precioCosto(this.precioCosto)
                .precioVenta(this.precioVenta)
                .cantidadMinima(this.cantidadMinima)
                .estado(this.estado)
                .version(this.version)
                .categoria(this.categoria != null && this.categoria.getId() != null
                        ? Categoria.builder().id(this.categoria.getId()).build()
                        : null)
                .build();
    }
}
