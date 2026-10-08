package com.almacen.stock_flow;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
  List<Movimiento> findAllByOrderByFechaDesc();

  List<Movimiento> findByFechaBetweenOrderByFechaDesc(LocalDateTime desde, LocalDateTime hasta);

  List<Movimiento> findByTipoOrderByFechaDesc(TipoMovimiento tipo);

  List<Movimiento> findByProductoIdOrderByFechaDesc(Long productoId);
}
