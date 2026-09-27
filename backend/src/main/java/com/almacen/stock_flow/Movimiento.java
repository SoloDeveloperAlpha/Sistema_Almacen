package com.almacen.stock_flow;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "movimientos")
public class Movimiento {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private LocalDateTime fecha;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private TipoMovimiento tipo;

  @Column(nullable = false)
  private int cantidad;

  @Column(length = 80)
  private String motivo;

  @Column(length = 150)
  private String tercero;

  @Column(length = 500)
  private String observaciones;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "producto_id", nullable = false)
  private Producto producto;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;

  protected Movimiento() {
  }

  public Movimiento(TipoMovimiento tipo, int cantidad, String motivo, String tercero,
      String observaciones, Producto producto, Usuario usuario) {
    this.fecha = LocalDateTime.now();
    this.tipo = tipo;
    this.cantidad = cantidad;
    this.motivo = motivo;
    this.tercero = tercero;
    this.observaciones = observaciones;
    this.producto = producto;
    this.usuario = usuario;
  }

  public Long getId() {
    return id;
  }

  public LocalDateTime getFecha() {
    return fecha;
  }

  public TipoMovimiento getTipo() {
    return tipo;
  }

  public int getCantidad() {
    return cantidad;
  }

  public String getMotivo() {
    return motivo;
  }

  public String getTercero() {
    return tercero;
  }

  public String getObservaciones() {
    return observaciones;
  }

  public Producto getProducto() {
    return producto;
  }

  public Usuario getUsuario() {
    return usuario;
  }
}
