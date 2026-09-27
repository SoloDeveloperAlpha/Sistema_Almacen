package com.almacen.stock_flow;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Index;

@Entity
@Table(name = "productos", uniqueConstraints = @UniqueConstraint(columnNames = "codigo"), indexes = {
    @Index(name = "idx_productos_activo_categoria", columnList = "activo, categoria")
})
public class Producto {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 40)
  private String codigo;

  @Column(nullable = false, length = 150)
  private String nombre;

  @Column(nullable = false, length = 80)
  private String categoria;

  @Column(nullable = false, length = 20)
  private String unidadMedida;

  @Column(nullable = false)
  private int stockActual;

  @Column(nullable = false)
  private int stockMinimo;

  @Column(length = 100)
  private String ubicacion;

  @Column(length = 150)
  private String proveedor;

  @Column(length = 500)
  private String descripcion;

  @Column(nullable = false)
  private boolean activo = true;

  protected Producto() {
  }

  public Producto(String codigo, String nombre, String categoria, String unidadMedida,
      int stockMinimo, String ubicacion, String proveedor, String descripcion) {
    this.codigo = codigo;
    this.nombre = nombre;
    this.categoria = categoria;
    this.unidadMedida = unidadMedida;
    this.stockMinimo = stockMinimo;
    this.ubicacion = ubicacion;
    this.proveedor = proveedor;
    this.descripcion = descripcion;
  }

  public Long getId() {
    return id;
  }

  public String getCodigo() {
    return codigo;
  }

  public String getNombre() {
    return nombre;
  }

  public String getCategoria() {
    return categoria;
  }

  public String getUnidadMedida() {
    return unidadMedida;
  }

  public int getStockActual() {
    return stockActual;
  }

  public int getStockMinimo() {
    return stockMinimo;
  }

  public String getUbicacion() {
    return ubicacion;
  }

  public String getProveedor() {
    return proveedor;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public boolean isActivo() {
    return activo;
  }

  public void sumarStock(int cantidad) {
    stockActual += cantidad;
  }

  public void restarStock(int cantidad) {
    stockActual -= cantidad;
  }
}
