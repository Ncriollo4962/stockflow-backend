package com.stockflow.core.service.impl;

import com.stockflow.core.dto.CategoriaDto;
import com.stockflow.core.entity.Categoria;
import com.stockflow.core.exception.ConflictException;
import com.stockflow.core.exception.ValidationException;
import com.stockflow.core.repository.CategoriaRepository;
import com.stockflow.core.service.CategoriaService;
import com.stockflow.core.utils.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoryRepository;

    @Override
    @Transactional
    public CategoriaDto insert(CategoriaDto categoriaDto) {

        validarCamposRequeridosCategoria(categoriaDto);

        Categoria newCategory = categoriaDto.toEntity();
        newCategory.setId(null);
        newCategory.setVersion(null);
        if (newCategory.getEstado() == null) {
            newCategory.setEstado(true);
        }

        Categoria savedCategory = categoryRepository.save(newCategory);

        return CategoriaDto.build().fromEntity(savedCategory);
    }

    @Override
    @Transactional
    public CategoriaDto update(CategoriaDto categoriaDto) {

        validarCamposRequeridosCategoria(categoriaDto);

        Categoria categoryToUpdate = categoryRepository.findById(categoriaDto.getId())
                .orElseThrow(() -> new ValidationException("Categoría no encontrada con ID: " + categoriaDto.getId()));

        validateVersion(categoriaDto, categoryToUpdate);

        categoryToUpdate.setCodigo(categoriaDto.getCodigo());
        categoryToUpdate.setNombre(categoriaDto.getNombre());
        categoryToUpdate.setDescripcion(categoriaDto.getDescripcion());
        categoryToUpdate.setEstado(categoriaDto.getEstado());

        Categoria savedCategory = categoryRepository.saveAndFlush(categoryToUpdate);
        return CategoriaDto.build().fromEntity(savedCategory);
    }

    private static void validarCamposRequeridosCategoria(CategoriaDto dto) {
        ValidationUtil.isRequired(dto.getCodigo(), "El código de la categoría es obligatorio.");
        ValidationUtil.isRequired(dto.getNombre(), "El nombre de la categoría es obligatorio.");
    }

    private static void validateVersion(CategoriaDto categoriaDto, Categoria categoriaBD) {
        if (categoriaDto.getVersion() != null && !categoriaBD.getVersion().equals(categoriaDto.getVersion())) {
            CategoriaDto actual = CategoriaDto.build().fromEntity(new CategoriaDto(), categoriaBD);
            throw new ConflictException("La categoria ha sido modificado por otro administrador. Por favor, recargue la página", actual);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public List<CategoriaDto> findAll() {
        return categoryRepository.findAll().stream()
                .map(category -> CategoriaDto.build().fromEntity(category))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaDto findById(Integer id) {
        return categoryRepository.findById(id)
                .map(category -> CategoriaDto.build().fromEntity(category))
                .orElseThrow(() -> new ValidationException("Categoría no encontrada con ID: " + id));
    }

    @Override
    @Transactional
    public void delete(CategoriaDto categoriaDto) {

        if (categoryRepository.existsById(categoriaDto.getId())) {
            categoryRepository.deleteById(categoriaDto.getId());
        } else {
            throw new ValidationException("No se encontró la Categoría con ID: " + categoriaDto.getId());
        }

    }
}
