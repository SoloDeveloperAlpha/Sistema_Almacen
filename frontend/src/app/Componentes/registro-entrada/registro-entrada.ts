import { afterNextRender, ChangeDetectorRef, Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { InventarioService } from '../../Servicios/inventario.service';
import { ErrorTemporal } from '../../Servicios/error-temporal';

@Component({
  imports: [RouterLink, FormsModule],
  selector: 'app-registro-entrada',
  styleUrl: './registro-entrada.css',
  templateUrl: './registro-entrada.html',
})
export class RegistroEntrada {
  datos = { codigo: '', nombre: '', categoria: '', unidadMedida: 'UNIDAD', cantidad: 1, stockMinimo: 1, ubicacion: '', proveedor: '', observaciones: '' };
  error = '';
  guardando = false;
  cargandoCodigo = true;
  private readonly inventario = inject(InventarioService);
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
          this.datos.codigo = inicial.siguienteCodigo;
          this.cargandoCodigo = false;
          this.changeDetector.markForCheck();
        },
        error: () => {
          this.mostrarError('No se pudo generar el código del producto.');
          this.cargandoCodigo = false;
          this.changeDetector.markForCheck();
        },
      });
    });
  }

  guardar(): void {
    this.errorTemporal.limpiar((valor) => (this.error = valor));
    this.guardando = true;
    this.inventario.registrarEntrada(this.datos).subscribe({
      next: () => { this.inventario.invalidarInicial(); this.router.navigateByUrl('/inventario'); },
      error: (error) => { this.mostrarError(error.status === 400 ? 'Completa los datos de la entrada.' : 'No se pudo registrar la entrada.'); this.guardando = false; },
    });
  }
}
