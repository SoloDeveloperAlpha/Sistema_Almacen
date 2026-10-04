package com.almacen.stock_flow;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
  Optional<Producto> findByCodigoIgnoreCase(String codigo);

  List<Producto> findByActivoTrueAndNombreContainingIgnoreCaseOrActivoTrueAndCodigoContainingIgnoreCase(
      String nombre, String codigo);

  Optional<Producto> findByActivoTrueAndNombreIgnoreCaseAndCategoriaIgnoreCaseAndProveedorIgnoreCase(
      String nombre, String categoria, String proveedor);

  List<Producto> findByCodigoStartingWithIgnoreCase(String prefijo);
}
