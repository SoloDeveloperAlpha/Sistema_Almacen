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
    return ResponseEntity.ok(new LoginResponse(usuario.getNombre()));
  }

  @PostMapping("/registro")
  public ResponseEntity<LoginResponse> registrar(@Valid @RequestBody RegistroRequest request) {
    Usuario usuario = usuarioService.registrar(request.usuario(), request.nombre(), request.contrasena());
    return ResponseEntity.status(HttpStatus.CREATED).body(new LoginResponse(usuario.getNombre()));
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

  public record LoginResponse(String nombre) {
  }
}
