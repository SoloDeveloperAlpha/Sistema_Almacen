package com.almacen.stock_flow;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// Spring Data JPA genera la implementacion de este repositorio al iniciar la aplicacion.
// JpaRepository aporta operaciones CRUD para Producto, cuya clave primaria es Long.
public interface ProductoRepository extends JpaRepository<Producto, Long> {
  // Busca por codigo sin distinguir mayusculas y minusculas.
  Optional<Producto> findByCodigoIgnoreCase(String codigo);

  // Busca productos activos cuyo nombre o codigo contenga el texto indicado.
  List<Producto> findByActivoTrueAndNombreContainingIgnoreCaseOrActivoTrueAndCodigoContainingIgnoreCase(
      String nombre, String codigo);

  // Encuentra un producto activo por nombre, categoria y proveedor, ignorando mayusculas.
  Optional<Producto> findByActivoTrueAndNombreIgnoreCaseAndCategoriaIgnoreCaseAndProveedorIgnoreCase(
      String nombre, String categoria, String proveedor);

  // Obtiene productos cuyo codigo comienza con el prefijo indicado.
  List<Producto> findByCodigoStartingWithIgnoreCase(String prefijo);
}
