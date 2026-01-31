package com.stockflow.core.service;

import com.stockflow.core.dto.UsuarioDto;
import com.stockflow.core.entity.Usuario;
import com.stockflow.core.utils.common.GenericCrud;

public interface UsuarioService  extends GenericCrud<UsuarioDto, Usuario, Integer> {
    UsuarioDto findByNameUser(String email);

}
