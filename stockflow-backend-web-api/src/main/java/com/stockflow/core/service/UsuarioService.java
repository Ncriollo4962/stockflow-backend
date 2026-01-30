package com.stockflow.core.service;

import com.stockflow.core.dto.UsuarioDto;

import java.util.List;

public interface UsuarioService {
    UsuarioDto save(UsuarioDto userDto);
    UsuarioDto findByEmail(String email);
    List<UsuarioDto> findAll();
}
