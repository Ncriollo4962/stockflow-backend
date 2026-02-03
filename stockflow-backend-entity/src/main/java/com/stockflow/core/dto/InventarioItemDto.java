package com.stockflow.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.stockflow.core.entity.EntityDto;
import com.stockflow.core.entity.InventarioItem;
import com.stockflow.core.entity.Producto;
import com.stockflow.core.entity.Ubicacion;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = false)
public class InventarioItemDto extends RepresentationModel<InventarioItemDto> implements EntityDto<InventarioItem, InventarioItemDto>, Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private ProductoDto producto;
    private UbicacionDto ubicacion;
    private String lote;
    private LocalDate fechaVencimiento;
    private Integer cantidad;
    private Integer cantidadReservada;
    private LocalDateTime fechaUltimoConteo;
    private Integer version;

    // --- Banderas de control (Template) ---
    @JsonIgnore
    @Builder.Default
    private boolean defId = true;
    @JsonIgnore
    @Builder.Default
    private boolean defLote = true;
    @JsonIgnore
    @Builder.Default
    private boolean defFechaVencimiento = true;
    @JsonIgnore
    @Builder.Default
    private boolean defCantidad = true;
    @JsonIgnore
    @Builder.Default
    private boolean defCantidadReservada = true;
    @JsonIgnore
    @Builder.Default
    private boolean defFechaUltimoConteo = true;
    @JsonIgnore
    @Builder.Default
    private boolean defVersion = true;

    @JsonIgnore
    @Builder.Default
    private ProductoDto defProducto = ProductoDto.build();

    @JsonIgnore
    @Builder.Default
    private UbicacionDto defUbicacion = UbicacionDto.build();

    public static InventarioItemDto build() {
        return InventarioItemDto.builder().build();
    }

    @Override
    public InventarioItemDto fromEntity(InventarioItem entity) {
        return fromEntity(this, entity);
    }

    @Override
    public InventarioItemDto fromEntity(InventarioItemDto template, InventarioItem entity) {
        if (entity == null) return null;
        InventarioItemDto dto = InventarioItemDto.builder().build();

        if (template.isDefId()) dto.setId(entity.getId());
        if (template.isDefLote()) dto.setLote(entity.getLote());
        if (template.isDefFechaVencimiento()) dto.setFechaVencimiento(entity.getFechaVencimiento());
        if (template.isDefCantidad()) dto.setCantidad(entity.getCantidad());
        if (template.isDefCantidadReservada()) dto.setCantidadReservada(entity.getCantidadReservada());
        if (template.isDefFechaUltimoConteo()) dto.setFechaUltimoConteo(entity.getFechaUltimoConteo());
        if (template.isDefVersion()) dto.setVersion(entity.getVersion());

        if (template.getDefProducto() != null && entity.getProducto() != null) {
            dto.setProducto(ProductoDto.build().fromEntity(template.getDefProducto(), entity.getProducto()));
        }

        if (template.getDefUbicacion() != null && entity.getUbicacion() != null) {
            dto.setUbicacion(UbicacionDto.build().fromEntity(template.getDefUbicacion(), entity.getUbicacion()));
        }

        return dto;
    }

    @Override
    public InventarioItem toEntity() {
        return InventarioItem.builder()
                .id(this.id)
                .lote(this.lote)
                .fechaVencimiento(this.fechaVencimiento)
                .cantidad(this.cantidad)
                .cantidadReservada(this.cantidadReservada)
                .fechaUltimoConteo(this.fechaUltimoConteo)
                .version(this.version)
                .producto(this.producto != null && this.producto.getId() != null
                        ? Producto.builder().id(this.producto.getId()).build()
                        : null)
                .ubicacion(this.ubicacion != null && this.ubicacion.getId() != null
                        ? Ubicacion.builder().id(this.ubicacion.getId()).build()
                        : null)
                .build();
    }
}
