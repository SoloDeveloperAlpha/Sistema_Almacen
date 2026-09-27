package com.almacen.stock_flow;

import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.time.LocalDateTime;

@Service
public class UsuarioService {

  private final UsuarioRepository usuarioRepository;
  private final PasswordEncoder passwordEncoder;
  private final String usuarioAdministradorInicial;
  private final long duracionTokenHoras;

  public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
      @Value("${app.bootstrap-admin-username:}") String usuarioAdministradorInicial,
      @Value("${app.session-duration-hours:8}") long duracionTokenHoras) {
    this.usuarioRepository = usuarioRepository;
    this.passwordEncoder = passwordEncoder;
    this.usuarioAdministradorInicial = usuarioAdministradorInicial.trim().toLowerCase(Locale.ROOT);
    this.duracionTokenHoras = Math.max(1, duracionTokenHoras);
  }

  public Usuario autenticar(String usuario, String contrasena) {
    Usuario registro = usuarioRepository.findByUsuarioIgnoreCase(usuario.trim())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

    if (!registro.isActivo() || !passwordEncoder.matches(contrasena, registro.getContrasena())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    registro.renovarToken();
    usuarioRepository.save(registro);
    return registro;
  }

  @Transactional
  public Usuario registrar(String usuario, String nombre, String contrasena) {
    String usuarioNormalizado = usuario.trim().toLowerCase(Locale.ROOT);
    if (usuarioRepository.existsByUsuarioIgnoreCase(usuarioNormalizado)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de usuario ya está registrado.");
    }

    Rol rol = usuarioNormalizado.equals(usuarioAdministradorInicial) ? Rol.ADMINISTRADOR : Rol.OPERATIVO;
    return usuarioRepository.save(new Usuario(
        usuarioNormalizado,
        nombre.trim(),
        passwordEncoder.encode(contrasena), rol));
  }

  public Usuario buscarActivoPorToken(String authorization) {
    String token = authorization != null && authorization.startsWith("Bearer ")
        ? authorization.substring(7).trim()
        : "";
    return usuarioRepository.findByTokenSesion(token)
        .filter(Usuario::isActivo)
        .filter(this::tokenVigente)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado."));
  }

  private boolean tokenVigente(Usuario usuario) {
    return usuario.getTokenCreadoEn() != null
        && LocalDateTime.now().isBefore(usuario.getTokenCreadoEn().plusHours(duracionTokenHoras));
  }

  @Transactional
  public void cerrarSesion(String authorization) {
    String token = authorization != null && authorization.startsWith("Bearer ")
        ? authorization.substring(7).trim()
        : "";
    usuarioRepository.findByTokenSesion(token).ifPresent(usuario -> {
      usuario.invalidarToken();
      usuarioRepository.save(usuario);
    });
  }

  public Usuario exigirAdministradorPorToken(String authorization) {
    Usuario registro = buscarActivoPorToken(authorization);
    if (registro.getRol() != Rol.ADMINISTRADOR) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere rol administrador.");
    }
    return registro;
  }

  public List<Usuario> listar() {
    return usuarioRepository.findAll();
  }

  @Transactional
  public Usuario actualizar(Long id, Rol rol, Boolean activo, String nombre) {
    Usuario registro = usuarioRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));
    if (rol != null)
      registro.cambiarRol(rol);
    if (activo != null)
      registro.cambiarEstado(activo);
    if (nombre != null && !nombre.isBlank())
      registro.cambiarNombre(nombre.trim());
    return usuarioRepository.save(registro);
  }
}
