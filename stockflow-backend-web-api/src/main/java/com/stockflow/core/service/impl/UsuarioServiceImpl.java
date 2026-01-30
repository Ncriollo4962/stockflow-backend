package com.stockflow.core.service.impl;

import com.stockflow.core.dto.UsuarioDto;
import com.stockflow.core.entity.Usuario;
import com.stockflow.core.repository.UsuarioRepository;
import com.stockflow.core.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository userRepository;

    @Override
    @Transactional
    public UsuarioDto save(UsuarioDto userDto) {

        Usuario user = userDto.toEntity();
        Usuario savedUser = userRepository.save(user);

        return UsuarioDto.build().fromEntity(savedUser);
    }

    @Override
    public UsuarioDto findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(user -> UsuarioDto.build().fromEntity(user))
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDto> findAll() {
        return userRepository.findAll().stream()
                .map(user -> UsuarioDto.build().fromEntity(user))
                .collect(Collectors.toList());
    }
}
