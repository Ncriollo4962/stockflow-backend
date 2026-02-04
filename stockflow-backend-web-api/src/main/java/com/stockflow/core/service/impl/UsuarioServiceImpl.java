package com.stockflow.core.service.impl;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stockflow.core.dto.UsuarioDto;
import com.stockflow.core.entity.Usuario;
import com.stockflow.core.exception.ConflictException;
import com.stockflow.core.exception.ValidationException;
import com.stockflow.core.repository.UsuarioRepository;
import com.stockflow.core.service.UsuarioService;
import com.stockflow.core.utils.ValidationUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UsuarioDto insert(UsuarioDto usuarioDto) {

        validarCamposBaseUsuario(usuarioDto, false);

        Usuario user = usuarioDto.toEntity();
   
        if (usuarioDto.getContrasena() != null) {
            user.setContrasena(passwordEncoder.encode(usuarioDto.getContrasena()));
        }

        user.setId(null);
        user.setVersion(null);
        if (user.getEstado() == null) {
            user.setEstado(true);
        }

        Usuario savedUser = userRepository.save(user);

        return UsuarioDto.build().fromEntity(savedUser);
    }

    @Override
    @Transactional
    public UsuarioDto update(UsuarioDto usuarioDto) {

        validarCamposBaseUsuario(usuarioDto, true);

        Usuario userToUpdate = userRepository.findById(usuarioDto.getId())
                .orElseThrow(() -> new ValidationException("Usuario no encontrado con ID: " + usuarioDto.getId()));

        validateVersion(usuarioDto, userToUpdate);

        userToUpdate.setCodigo(usuarioDto.getCodigo());
        userToUpdate.setNombre(usuarioDto.getNombre());
        userToUpdate.setEmail(usuarioDto.getEmail());
        userToUpdate.setRol(usuarioDto.getRol().toUpperCase());
        userToUpdate.setEstado(usuarioDto.getEstado());

        if (usuarioDto.getContrasena() != null && !usuarioDto.getContrasena().isBlank()) {
            String encodedPassword = passwordEncoder.encode(usuarioDto.getContrasena());
            userToUpdate.setContrasena(encodedPassword);
        }

        Usuario savedUser = userRepository.saveAndFlush(userToUpdate);

        return UsuarioDto.build().fromEntity(savedUser);
    }

    @Override
    @Transactional
    public void delete(UsuarioDto usuarioDto) {

        if (userRepository.existsById(usuarioDto.getId())) {
            userRepository.deleteById(usuarioDto.getId());
        } else {
            throw new ValidationException("No se encontró el usuario con ID: " + usuarioDto.getId());
        }

    }

    private static void validarCamposBaseUsuario(UsuarioDto dto, boolean esUpdate) {
        ValidationUtil.isRequired(dto.getCodigo(), "El código de usuario es obligatorio.");
        ValidationUtil.isRequired(dto.getNombre(), "El nombre es obligatorio.");
        ValidationUtil.isRequired(dto.getEmail(), "El correo electrónico es requerido.");
        ValidationUtil.isRequired(dto.getRol(), "Debe asignar un rol al usuario.");

        // La contraseña solo es estrictamente obligatoria al crear
        if (!esUpdate) {
            ValidationUtil.isRequired(dto.getContrasena(), "La contraseña es obligatoria para nuevos usuarios.");
        }
    }

    private static void validateVersion(UsuarioDto usuarioDto, Usuario usuarioBD) {
        if (usuarioDto.getVersion() != null && !usuarioBD.getVersion().equals(usuarioDto.getVersion())) {
            UsuarioDto actual = UsuarioDto.build().fromEntity(new UsuarioDto(), usuarioBD);
            throw new ConflictException(
                    "El usuario ha sido modificado por otro administrador. Por favor, recargue la página", actual);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioDto findById(Integer id) {
        return userRepository.findById(id)
                .map(user -> UsuarioDto.build().fromEntity(user))
                .orElseThrow(() -> new ValidationException("Usuario no encontrado con ID: " + id));
    }

    @Override
    public UsuarioDto findByNameUser(String email) {
        return userRepository.findByEmail(email)
                .map(user -> UsuarioDto.build().fromEntity(user))
                .orElseThrow(() -> new ValidationException("Usuario no encontrado con EMAIL: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDto> findAll() {
        return userRepository.findAll().stream()
                .map(user -> UsuarioDto.build().fromEntity(user))
                .toList();
    }

}
