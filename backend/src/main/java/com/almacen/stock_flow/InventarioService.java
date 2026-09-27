package com.almacen.stock_flow;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InventarioService {
  private final ProductoRepository productoRepository;
  private final MovimientoRepository movimientoRepository;
  private final UsuarioService usuarioService;

  public InventarioService(ProductoRepository productoRepository, MovimientoRepository movimientoRepository,
      UsuarioService usuarioService) {
    this.productoRepository = productoRepository;
    this.movimientoRepository = movimientoRepository;
    this.usuarioService = usuarioService;
  }

  public List<Producto> buscarProductos(String buscar, String categoria) {
    String texto = buscar == null ? "" : buscar.trim().toLowerCase(Locale.ROOT);
    return productoRepository.findAll().stream()
        .filter(Producto::isActivo)
        .filter(producto -> texto.isBlank()
            || producto.getNombre().toLowerCase(Locale.ROOT).contains(texto)
            || producto.getCodigo().toLowerCase(Locale.ROOT).contains(texto))
        .filter(producto -> categoria == null || categoria.isBlank()
            || producto.getCategoria().equalsIgnoreCase(categoria))
        .toList();
  }

  public List<String> categorias() {
    return productoRepository.findAll().stream().filter(Producto::isActivo)
        .map(Producto::getCategoria).distinct().sorted().toList();
  }

  @Transactional
  public Movimiento registrarEntrada(EntradaRequest request, String authorization) {
    Usuario actor = usuarioService.buscarActivoPorToken(authorization);
    if (request.cantidad() <= 0)
      throw badRequest("La cantidad debe ser mayor que cero.");
    Producto producto = productoRepository.findByCodigoIgnoreCase(request.codigo().trim())
        .orElseGet(() -> new Producto(request.codigo().trim(), request.nombre().trim(), request.categoria().trim(),
            request.unidadMedida().trim(), request.stockMinimo(), request.ubicacion(), request.proveedor(),
            request.observaciones()));
    producto.sumarStock(request.cantidad());
    Producto guardado = productoRepository.save(producto);
    return movimientoRepository
        .save(new Movimiento(TipoMovimiento.ENTRADA, request.cantidad(), "Recepcion de mercancia",
            request.proveedor(), request.observaciones(), guardado, actor));
  }

  @Transactional
  public Movimiento registrarSalida(SalidaRequest request, String authorization) {
    Usuario actor = usuarioService.buscarActivoPorToken(authorization);
    if (request.cantidad() <= 0)
      throw badRequest("La cantidad debe ser mayor que cero.");
    Producto producto = productoRepository.findById(request.productoId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado."));
    if (producto.getStockActual() < request.cantidad()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente para realizar la salida.");
    }
    producto.restarStock(request.cantidad());
    productoRepository.save(producto);
    return movimientoRepository.save(new Movimiento(TipoMovimiento.SALIDA, request.cantidad(), request.motivo(),
        request.destino(), request.observaciones(), producto, actor));
  }

  public List<Movimiento> buscarMovimientos(String tipo, LocalDate desde, LocalDate hasta, String buscar) {
    String texto = buscar == null ? "" : buscar.trim().toLowerCase(Locale.ROOT);
    TipoMovimiento tipoMovimiento;
    try {
      tipoMovimiento = tipo == null || tipo.isBlank() ? null : TipoMovimiento.valueOf(tipo.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException exception) {
      throw badRequest("El tipo de movimiento debe ser ENTRADA o SALIDA.");
    }
    LocalDateTime inicio = desde == null ? null : desde.atStartOfDay();
    LocalDateTime fin = hasta == null ? null : hasta.plusDays(1).atStartOfDay().minusNanos(1);
    return movimientoRepository.findAllByOrderByFechaDesc().stream()
        .filter(movimiento -> inicio == null || !movimiento.getFecha().isBefore(inicio))
        .filter(movimiento -> fin == null || !movimiento.getFecha().isAfter(fin))
        .filter(movimiento -> tipoMovimiento == null || movimiento.getTipo() == tipoMovimiento)
        .filter(movimiento -> texto.isBlank()
            || movimiento.getProducto().getNombre().toLowerCase(Locale.ROOT).contains(texto)
            || movimiento.getProducto().getCodigo().toLowerCase(Locale.ROOT).contains(texto))
        .toList();
  }

  public ReporteResumen resumen() {
    List<Producto> productos = productoRepository.findAll().stream().filter(Producto::isActivo).toList();
    return new ReporteResumen(productos.size(), productos.stream().mapToInt(Producto::getStockActual).sum(),
        productos.stream().filter(p -> p.getStockActual() > 0 && p.getStockActual() <= p.getStockMinimo()).count(),
        productos.stream().filter(p -> p.getStockActual() == 0).count(), movimientoRepository.count());
  }

  public String reporteInventarioCsv() {
    StringBuilder csv = new StringBuilder("codigo,nombre,categoria,unidad,stock_actual,stock_minimo,ubicacion\n");
    productoRepository.findAll().stream().filter(Producto::isActivo).forEach(producto -> csv
        .append(fila(producto.getCodigo(), producto.getNombre(), producto.getCategoria(), producto.getUnidadMedida(),
            producto.getStockActual(), producto.getStockMinimo(), producto.getUbicacion()))
        .append('\n'));
    return csv.toString();
  }

  public String reporteMovimientosCsv(String tipo, LocalDate desde, LocalDate hasta, String buscar) {
    StringBuilder csv = new StringBuilder(
        "fecha,tipo,producto_codigo,producto_nombre,cantidad,motivo,tercero,usuario\n");
    buscarMovimientos(tipo, desde, hasta, buscar).forEach(movimiento -> csv.append(fila(
        movimiento.getFecha(), movimiento.getTipo(), movimiento.getProducto().getCodigo(),
        movimiento.getProducto().getNombre(), movimiento.getCantidad(), movimiento.getMotivo(),
        movimiento.getTercero(), movimiento.getUsuario().getNombre())).append('\n'));
    return csv.toString();
  }

  private String fila(Object... valores) {
    return java.util.Arrays.stream(valores).map(valor -> {
      String texto = valor == null ? "" : valor.toString().replace("\"", "\"\"");
      return "\"" + texto + "\"";
    }).collect(java.util.stream.Collectors.joining(","));
  }

  private ResponseStatusException badRequest(String mensaje) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
  }

  public record EntradaRequest(@NotBlank String codigo, @NotBlank String nombre, @NotBlank String categoria,
      @NotBlank String unidadMedida, @Min(1) int cantidad, @Min(0) int stockMinimo,
      String ubicacion, String proveedor, String observaciones) {
  }

  public record SalidaRequest(@NotNull Long productoId, @Min(1) int cantidad, @NotBlank String motivo,
      String destino, String observaciones) {
  }

  public record ReporteResumen(long productos, long unidades, long stockBajo, long agotados, long movimientos) {
  }
}
