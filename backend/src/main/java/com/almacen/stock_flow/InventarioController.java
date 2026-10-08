package com.almacen.stock_flow;

import java.time.LocalDate;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class InventarioController {
  private final InventarioService inventarioService;
  private final UsuarioService usuarioService;

  public InventarioController(InventarioService inventarioService, UsuarioService usuarioService) {
    this.inventarioService = inventarioService;
    this.usuarioService = usuarioService;
  }

  @GetMapping("/productos")
  public List<Producto> productos(@RequestHeader("Authorization") String authorization,
      @RequestParam(required = false) String buscar, @RequestParam(required = false) String categoria) {
    usuarioService.buscarActivoPorToken(authorization);
    return inventarioService.buscarProductos(buscar, categoria);
  }

  @GetMapping("/productos/categorias")
  public List<String> categorias(@RequestHeader("Authorization") String authorization) {
    usuarioService.buscarActivoPorToken(authorization);
    return inventarioService.categorias();
  }

  @GetMapping("/inventario/inicial")
  public InventarioService.InventarioInicial inicial(@RequestHeader("Authorization") String authorization) {
    usuarioService.buscarActivoPorToken(authorization);
    return inventarioService.inicial();
  }

  @PostMapping("/movimientos/entrada")
  public ResponseEntity<MovimientoResponse> entrada(@RequestHeader("Authorization") String authorization,
      @Valid @RequestBody InventarioService.EntradaRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(MovimientoResponse.desde(inventarioService.registrarEntrada(request, authorization)));
  }

  @PostMapping("/movimientos/salida")
  public ResponseEntity<MovimientoResponse> salida(@RequestHeader("Authorization") String authorization,
      @Valid @RequestBody InventarioService.SalidaRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(MovimientoResponse.desde(inventarioService.registrarSalida(request, authorization)));
  }

  @GetMapping("/movimientos")
  public List<MovimientoResponse> movimientos(@RequestHeader("Authorization") String authorization,
      @RequestParam(required = false) String tipo,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
      @RequestParam(required = false) String buscar) {
    usuarioService.buscarActivoPorToken(authorization);
    return inventarioService.buscarMovimientos(tipo, desde, hasta, buscar).stream().map(MovimientoResponse::desde)
        .toList();
  }

  @GetMapping("/reportes/resumen")
  public InventarioService.ReporteResumen resumen(@RequestHeader("Authorization") String authorization) {
    usuarioService.buscarActivoPorToken(authorization);
    return inventarioService.resumen();
  }

  @GetMapping(value = "/reportes/inventario.csv", produces = "text/csv")
  public ResponseEntity<String> reporteInventario(@RequestHeader("Authorization") String authorization) {
    usuarioService.buscarActivoPorToken(authorization);
    return csvResponse("inventario.csv", inventarioService.reporteInventarioCsv());
  }

  @GetMapping(value = "/reportes/movimientos.csv", produces = "text/csv")
  public ResponseEntity<String> reporteMovimientos(@RequestHeader("Authorization") String authorization,
      @RequestParam(required = false) String tipo,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
      @RequestParam(required = false) String buscar) {
    usuarioService.buscarActivoPorToken(authorization);
    return csvResponse("movimientos.csv", inventarioService.reporteMovimientosCsv(tipo, desde, hasta, buscar));
  }

  private ResponseEntity<String> csvResponse(String nombre, String contenido) {
    return ResponseEntity.ok().contentType(MediaType.parseMediaType("text/csv"))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nombre).body(contenido);
  }

  public record MovimientoResponse(Long id, String fecha, TipoMovimiento tipo, int cantidad, String motivo,
      String tercero, String observaciones, Long productoId, String productoCodigo, String productoNombre,
      String usuario) {
    static MovimientoResponse desde(Movimiento movimiento) {
      return new MovimientoResponse(movimiento.getId(), movimiento.getFecha().toString(), movimiento.getTipo(),
          movimiento.getCantidad(), movimiento.getMotivo(), movimiento.getTercero(), movimiento.getObservaciones(),
          movimiento.getProducto().getId(), movimiento.getProducto().getCodigo(), movimiento.getProducto().getNombre(),
          movimiento.getUsuario().getNombre());
    }
  }
}
