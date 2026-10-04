import { afterNextRender, ChangeDetectorRef, Component, inject } from '@angular/core';
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
  guardando = false;
  cargandoProductos = true;
  private readonly inventario = inject(InventarioService);
  private readonly route = inject(ActivatedRoute, { optional: true });
  private readonly router = inject(Router);
  private readonly changeDetector = inject(ChangeDetectorRef);

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
    this.guardando = true;
    this.inventario.registrarSalida(this.datos).subscribe({
      next: () => { this.inventario.invalidarInicial(); this.router.navigateByUrl('/historial'); },
      error: () => {
        this.guardando = false;
        this.changeDetector.markForCheck();
      },
    });
  }
}
