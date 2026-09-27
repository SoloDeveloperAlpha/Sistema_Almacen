package com.almacen.stock_flow;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final UsuarioService usuarioService;

  public AuthController(UsuarioService usuarioService) {
    this.usuarioService = usuarioService;
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    Usuario usuario = usuarioService.autenticar(request.usuario(), request.contrasena());
    return ResponseEntity.ok(LoginResponse.desde(usuario));
  }

  @PostMapping("/registro")
  public ResponseEntity<LoginResponse> registrar(@Valid @RequestBody RegistroRequest request) {
    Usuario usuario = usuarioService.registrar(request.usuario(), request.nombre(), request.contrasena());
    return ResponseEntity.status(HttpStatus.CREATED).body(LoginResponse.desde(usuario));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization) {
    usuarioService.cerrarSesion(authorization);
    return ResponseEntity.noContent().build();
  }

  public record LoginRequest(
      @NotBlank @Size(max = 32) String usuario,
      @NotBlank String contrasena) {
  }

  public record RegistroRequest(
      @NotBlank @Size(min = 3, max = 32) @Pattern(regexp = "[a-zA-Z0-9._-]+") String usuario,
      @NotBlank @Size(min = 2, max = 100) String nombre,
      @NotBlank @Size(min = 8, max = 72) String contrasena) {
  }

  public record LoginResponse(String usuario, String nombre, Rol rol, String token) {
    static LoginResponse desde(Usuario usuario) {
      return new LoginResponse(usuario.getUsuario(), usuario.getNombre(), usuario.getRol(), usuario.getTokenSesion());
    }
  }
}
