package com.stockflow.core.service.impl;

import com.stockflow.core.dto.ProductoDto;
import com.stockflow.core.entity.Categoria;
import com.stockflow.core.entity.Producto;
import com.stockflow.core.exception.ConflictException;
import com.stockflow.core.exception.ValidationException;
import com.stockflow.core.repository.CategoriaRepository;
import com.stockflow.core.repository.ProductoRepository;
import com.stockflow.core.service.ProductoService;
import com.stockflow.core.utils.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productRepository;
    private final CategoriaRepository categoryRepository;

    @Override
    @Transactional
    public ProductoDto insert(ProductoDto productDto) {

        validarCamposBaseProducto(productDto);

        Producto newProducto = productDto.toEntity();

        newProducto.setId(null);
        newProducto.setVersion(null);
        if (productDto.getEstado() == null) {
            newProducto.setEstado(true);
        }

        asignarCategoria(productDto, newProducto);

        Producto savedProduct = productRepository.saveAndFlush(newProducto);

        return ProductoDto.build().fromEntity(productDto, savedProduct);
    }

    @Override
    @Transactional
    public ProductoDto update(ProductoDto productDto) {

        validarCamposBaseProducto(productDto);

        Producto productoBD = productRepository
                .findById(Objects.requireNonNull(productDto.getId(), "Product ID must not be null"))
                .orElseThrow(() -> new ValidationException("Producto no encontrado con ID: " + productDto.getId()));

        validateVersion(productDto, productoBD);

        productoBD.setCodigo(productDto.getCodigo());
        productoBD.setNombre(productDto.getNombre());
        productoBD.setDescripcion(productDto.getDescripcion());
        productoBD.setPrecioCosto(productDto.getPrecioCosto());
        productoBD.setPrecioVenta(productDto.getPrecioVenta());
        productoBD.setCantidadMinima(productDto.getCantidadMinima());
        productoBD.setEstado(productDto.getEstado());

        asignarCategoria(productDto, productoBD);

        Producto savedProduct = productRepository.saveAndFlush(productoBD);
        return ProductoDto.build().fromEntity(productDto, savedProduct);
    }

    private static void validateVersion(ProductoDto productDto, Producto productoBD) {
        if (productDto.getVersion() != null && !productoBD.getVersion().equals(productDto.getVersion())) {
            ProductoDto actual = ProductoDto.build().fromEntity(new ProductoDto(), productoBD);
            throw new ConflictException(
                    "El producto ha sido modificado por otro administrador. Por favor, recargue la página", actual);
        }
    }

    private void asignarCategoria(ProductoDto productDto, Producto productoBD) {

        if (productDto.getCategoria() != null && productDto.getCategoria().getId() != null) {
            Categoria nuevaCat = categoryRepository
                    .findById(Objects.requireNonNull(productDto.getCategoria().getId(), "Category ID must not be null"))
                    .orElseThrow(() -> new ValidationException(
                            "Categoría no encontrada con ID: " + productDto.getCategoria().getId()));
            productoBD.setCategoria(nuevaCat);

        }
    }

    private static void validarCamposBaseProducto(ProductoDto dto) {
        ValidationUtil.isRequired(dto.getCodigo(), "El código del producto es obligatorio.");
        ValidationUtil.isRequired(dto.getNombre(), "El nombre del producto es obligatorio.");
        ValidationUtil.isRequired(dto.getCategoria(), "La categoría es requerida.");
        ValidationUtil.isRequired(dto.getCategoria().getId(), "El ID de categoría es requerido.");
        ValidationUtil.isRequired(dto.getPrecioCosto(), "El precio de costo es requerido.");
        ValidationUtil.isRequired(dto.getPrecioVenta(), "El precio de venta es requerido.");
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Producto producto = productRepository.findById(Objects.requireNonNull(id, "Product ID must not be null"))
                .orElseThrow(() -> new ValidationException("No se encontró el producto con ID: " + id));

        producto.setEstado(false);
        productRepository.saveAndFlush(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoDto findById(Integer id) {
        return productRepository.findById(Objects.requireNonNull(id, "Product ID must not be null"))
                .map(product -> ProductoDto.build().fromEntity(product))
                .orElseThrow(() -> new ValidationException("Producto no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDto> findAll() {

        ProductoDto template = ProductoDto.build();

        return productRepository.findAll().stream()
                .map(product -> ProductoDto.build().fromEntity(template, product))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoDto findByCodigo(String codigo) {
        return productRepository.findByCodigo(codigo)
                .map(product -> ProductoDto.build().fromEntity(product))
                .orElse(null);
    }
}
