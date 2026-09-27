package com.almacen.stock_flow;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
  private final UsuarioService usuarioService;

  public UsuarioController(UsuarioService usuarioService) {
    this.usuarioService = usuarioService;
  }

  @GetMapping
  public List<UsuarioResponse> listar(@RequestHeader("Authorization") String authorization) {
    usuarioService.exigirAdministradorPorToken(authorization);
    return usuarioService.listar().stream().map(UsuarioResponse::desde).toList();
  }

  @PatchMapping("/{id}")
  public UsuarioResponse actualizar(@RequestHeader("Authorization") String authorization, @PathVariable Long id,
      @RequestBody ActualizarUsuarioRequest request) {
    usuarioService.exigirAdministradorPorToken(authorization);
    return UsuarioResponse.desde(usuarioService.actualizar(id, request.rol(), request.activo(), request.nombre()));
  }

  public record ActualizarUsuarioRequest(Rol rol, Boolean activo, String nombre) {
  }

  public record UsuarioResponse(Long id, String usuario, String nombre, Rol rol, boolean activo) {
    static UsuarioResponse desde(Usuario usuario) {
      return new UsuarioResponse(usuario.getId(), usuario.getUsuario(), usuario.getNombre(), usuario.getRol(),
          usuario.isActivo());
    }
  }
}
