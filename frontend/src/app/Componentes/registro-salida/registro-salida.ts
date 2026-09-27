import { afterNextRender, ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { InventarioService, Producto } from '../../Servicios/inventario.service';
import { ErrorTemporal } from '../../Servicios/error-temporal';

@Component({
  imports: [FormsModule, RouterLink],
  selector: 'app-registro-salida',
  styleUrl: './registro-salida.css',
  templateUrl: './registro-salida.html',
})
export class RegistroSalida {
  productos: Producto[] = [];
  datos = { productoId: 0, cantidad: 1, motivo: 'Despacho a cliente', destino: '', observaciones: '' };
  error = '';
  guardando = false;
  cargandoProductos = true;
  private readonly inventario = inject(InventarioService);
  private readonly route = inject(ActivatedRoute, { optional: true });
  private readonly router = inject(Router);
  private readonly changeDetector = inject(ChangeDetectorRef);
  private readonly errorTemporal = new ErrorTemporal();

  private mostrarError(mensaje: string): void {
    this.errorTemporal.mostrar(mensaje, (valor) => (this.error = valor));
  }

  constructor() {
    afterNextRender(() => {
      this.inventario.inicial().subscribe({
        next: (inicial) => {
          this.productos = inicial.productos;
          const productoId = Number(this.route?.snapshot.queryParamMap.get('productoId'));
          this.datos.productoId = this.productos.some((producto) => producto.id === productoId) ? productoId : (this.productos[0]?.id ?? 0);
          this.cargandoProductos = false;
          this.changeDetector.markForCheck();
        },
        error: () => {
          this.mostrarError('No se pudieron cargar los productos.');
          this.cargandoProductos = false;
          this.changeDetector.markForCheck();
        },
      });
    });
  }

  productoSeleccionado(): Producto | undefined {
    return this.productos.find((producto) => producto.id === Number(this.datos.productoId));
  }

  guardar(): void {
    this.errorTemporal.limpiar((valor) => (this.error = valor));
    this.guardando = true;
    this.inventario.registrarSalida(this.datos).subscribe({
      next: () => { this.inventario.invalidarInicial(); this.router.navigateByUrl('/historial'); },
      error: (error) => {
        this.mostrarError(error.status === 409 ? 'No hay stock suficiente para esta salida.' : 'No se pudo registrar la salida.');
        this.guardando = false;
        this.changeDetector.markForCheck();
      },
    });
  }
}
