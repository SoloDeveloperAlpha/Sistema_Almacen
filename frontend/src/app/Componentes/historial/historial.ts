import { afterNextRender, Component, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InventarioService, Movimiento } from '../../Servicios/inventario.service';
import { ErrorTemporal } from '../../Servicios/error-temporal';

@Component({
  imports: [FormsModule, DatePipe],
  selector: 'app-historial',
  styleUrl: './historial.css',
  templateUrl: './historial.html',
})
export class Historial {
  movimientos: Movimiento[] = [];
  filtros = { tipo: '', desde: '', hasta: '', buscar: '' };
  error = '';
  private readonly inventario = inject(InventarioService);
  private readonly errorTemporal = new ErrorTemporal();

  constructor() {
    afterNextRender(() => this.cargar());
  }

  cargar(): void {
    this.inventario.movimientos(this.filtros).subscribe({
      next: (movimientos) => (this.movimientos = movimientos),
      error: () => this.errorTemporal.mostrar('No se pudo consultar el historial.', (valor) => (this.error = valor)),
    });
  }

  descargarReporte(): void {
    this.inventario.reporteMovimientos(this.filtros).subscribe((archivo) => {
      const enlace = document.createElement('a');
      enlace.href = URL.createObjectURL(archivo);
      enlace.download = 'movimientos.csv';
      enlace.click();
      URL.revokeObjectURL(enlace.href);
    });
  }
}
