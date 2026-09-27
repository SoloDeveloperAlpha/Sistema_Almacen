import { afterNextRender, Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { InventarioService, Producto, ResumenInventario } from '../../Servicios/inventario.service';
import { ErrorTemporal } from '../../Servicios/error-temporal';

@Component({
  imports: [RouterLink, FormsModule],
  selector: 'app-inventario',
  styleUrl: './inventario.css',
  templateUrl: './inventario.html',
})
export class Inventario {
  productos: Producto[] = [];
  categorias: string[] = [];
  resumen: ResumenInventario = { productos: 0, unidades: 0, stockBajo: 0, agotados: 0, movimientos: 0 };
  buscar = '';
  categoria = '';
  error = '';
  cargando = false;

  private readonly inventario = inject(InventarioService);
  private readonly errorTemporal = new ErrorTemporal();

  private mostrarError(mensaje: string): void {
    this.errorTemporal.mostrar(mensaje, (valor) => (this.error = valor));
  }

  constructor() {
    afterNextRender(() => {
      this.cargando = true;
      this.inventario.inicial().subscribe({
        next: (inicial) => {
          this.productos = inicial.productos;
          this.categorias = inicial.categorias;
          this.resumen = inicial.resumen;
          this.cargando = false;
        },
        error: () => { this.mostrarError('No se pudo cargar el inventario.'); this.cargando = false; },
      });
    });
  }

  cargar(): void {
    this.cargando = true;
    this.errorTemporal.limpiar((valor) => (this.error = valor));
    this.inventario.productos(this.buscar, this.categoria).subscribe({
      next: (productos) => (this.productos = productos),
      error: () => this.mostrarError('No se pudo consultar el inventario.'),
      complete: () => (this.cargando = false),
    });
  }

  limpiar(): void {
    this.buscar = '';
    this.categoria = '';
    this.cargar();
  }

  estado(producto: Producto): string {
    if (producto.stockActual === 0) return 'Agotado';
    if (producto.stockActual <= producto.stockMinimo) return 'Stock bajo';
    return 'Disponible';
  }

  descargarReporte(): void {
    this.inventario.reporteInventario().subscribe((archivo) => this.descargar(archivo, 'inventario.csv'));
  }

  private descargar(archivo: Blob, nombre: string): void {
    const enlace = document.createElement('a');
    enlace.href = URL.createObjectURL(archivo);
    enlace.download = nombre;
    enlace.click();
    URL.revokeObjectURL(enlace.href);
  }
}
