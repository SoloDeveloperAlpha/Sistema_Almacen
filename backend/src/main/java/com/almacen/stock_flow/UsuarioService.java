package com.almacen.stock_flow;

import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {

  private final UsuarioRepository usuarioRepository;
  private final PasswordEncoder passwordEncoder;

  public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
    this.usuarioRepository = usuarioRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public Usuario autenticar(String usuario, String contrasena) {
    Usuario registro = usuarioRepository.findByUsuarioIgnoreCase(usuario.trim())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

    if (!passwordEncoder.matches(contrasena, registro.getContrasena())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    return registro;
  }

  @Transactional
  public Usuario registrar(String usuario, String nombre, String contrasena) {
    String usuarioNormalizado = usuario.trim().toLowerCase(Locale.ROOT);
    if (usuarioRepository.existsByUsuarioIgnoreCase(usuarioNormalizado)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de usuario ya está registrado.");
    }

    return usuarioRepository.save(new Usuario(
        usuarioNormalizado,
        nombre.trim(),
        passwordEncoder.encode(contrasena)));
  }
}
