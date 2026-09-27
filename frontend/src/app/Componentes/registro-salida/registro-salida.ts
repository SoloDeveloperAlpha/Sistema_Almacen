import { afterNextRender, Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { InventarioService, Producto } from '../../Servicios/inventario.service';

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
  private readonly inventario = inject(InventarioService);
  private readonly route = inject(ActivatedRoute, { optional: true });
  private readonly router = inject(Router);

  constructor() {
    afterNextRender(() => {
      this.inventario.productos().subscribe({
        next: (productos) => {
          this.productos = productos;
          const productoId = Number(this.route?.snapshot.queryParamMap.get('productoId'));
          this.datos.productoId = productos.some((producto) => producto.id === productoId) ? productoId : (productos[0]?.id ?? 0);
        },
        error: () => (this.error = 'No se pudieron cargar los productos.'),
      });
    });
  }

  productoSeleccionado(): Producto | undefined {
    return this.productos.find((producto) => producto.id === Number(this.datos.productoId));
  }

  guardar(): void {
    this.error = '';
    this.guardando = true;
    this.inventario.registrarSalida(this.datos).subscribe({
      next: () => this.router.navigateByUrl('/historial'),
      error: (error) => { this.error = error.status === 409 ? 'No hay stock suficiente para esta salida.' : 'No se pudo registrar la salida.'; this.guardando = false; },
    });
  }
}
