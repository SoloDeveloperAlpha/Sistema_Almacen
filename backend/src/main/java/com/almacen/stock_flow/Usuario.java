package com.almacen.stock_flow;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

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

  protected Usuario() {
  }

  public Usuario(String usuario, String nombre, String contrasena) {
    this.usuario = usuario;
    this.nombre = nombre;
    this.contrasena = contrasena;
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
}
