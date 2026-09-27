package com.almacen.stock_flow;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios", uniqueConstraints = @UniqueConstraint(columnNames = "usuario"))
public class Usuario {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 32)
  private String usuario;

  @Column(nullable = false, length = 100)
  private String nombre;

  @Column(nullable = false, length = 60)
  private String contrasena;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Rol rol;

  @Column(nullable = false)
  private boolean activo = true;

  @Column(length = 36, unique = true)
  private String tokenSesion;

  @Column
  private LocalDateTime tokenCreadoEn;

  protected Usuario() {
  }

  public Usuario(String usuario, String nombre, String contrasena) {
    this(usuario, nombre, contrasena, Rol.OPERATIVO);
  }

  public Usuario(String usuario, String nombre, String contrasena, Rol rol) {
    this.usuario = usuario;
    this.nombre = nombre;
    this.contrasena = contrasena;
    this.rol = rol;
    this.tokenSesion = UUID.randomUUID().toString();
    this.tokenCreadoEn = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public String getUsuario() {
    return usuario;
  }

  public String getNombre() {
    return nombre;
  }

  public String getContrasena() {
    return contrasena;
  }

  public Rol getRol() {
    return rol;
  }

  public boolean isActivo() {
    return activo;
  }

  public String getTokenSesion() {
    return tokenSesion;
  }

  public LocalDateTime getTokenCreadoEn() {
    return tokenCreadoEn;
  }

  public void renovarToken() {
    this.tokenSesion = UUID.randomUUID().toString();
    this.tokenCreadoEn = LocalDateTime.now();
  }

  public void invalidarToken() {
    this.tokenSesion = null;
    this.tokenCreadoEn = null;
  }

  public void cambiarNombre(String nombre) {
    this.nombre = nombre;
  }

  public void cambiarRol(Rol rol) {
    this.rol = rol;
  }

  public void cambiarEstado(boolean activo) {
    this.activo = activo;
  }
}
