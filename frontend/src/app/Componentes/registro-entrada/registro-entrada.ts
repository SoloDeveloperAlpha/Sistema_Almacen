import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { InventarioService } from '../../Servicios/inventario.service';

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
  private readonly inventario = inject(InventarioService);
  private readonly router = inject(Router);

  guardar(): void {
    this.error = '';
    this.guardando = true;
    this.inventario.registrarEntrada(this.datos).subscribe({
      next: () => this.router.navigateByUrl('/inventario'),
      error: (error) => { this.error = error.status === 400 ? 'Completa los datos de la entrada.' : 'No se pudo registrar la entrada.'; this.guardando = false; },
    });
  }
}
