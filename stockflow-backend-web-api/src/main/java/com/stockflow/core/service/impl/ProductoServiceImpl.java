package com.stockflow.core.service.impl;

import com.stockflow.core.dto.ProductoDto;
import com.stockflow.core.entity.Categoria;
import com.stockflow.core.entity.Producto;
import com.stockflow.core.repository.CategoriaRepository;
import com.stockflow.core.repository.ProductoRepository;
import com.stockflow.core.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productRepository;
    private final CategoriaRepository categoryRepository;

    @Override
    @Transactional
    public ProductoDto save(ProductoDto productDto) {

        Producto product = productDto.toEntity();

        if (productDto.getCategoria() != null && productDto.getCategoria().getId() != null) {
            Categoria category = categoryRepository.findById(productDto.getCategoria().getId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + productDto.getCategoria().getId()));
            product.setCategoria(category);
        }

        Producto savedProduct = productRepository.save(product);

        return ProductoDto.build().fromEntity(productDto, savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoDto findById(Integer id) {
        return productRepository.findById(id)
                .map(product -> ProductoDto.build().fromEntity(product))
                .orElse(null);
    }

    @Override
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
