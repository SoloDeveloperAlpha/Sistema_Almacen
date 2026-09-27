import { afterNextRender, Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { InventarioService, UsuarioAdministrado } from '../../Servicios/inventario.service';
import { ErrorTemporal } from '../../Servicios/error-temporal';

@Component({
  imports: [FormsModule],
  selector: 'app-usuarios',
  templateUrl: './usuarios.html',
})
export class Usuarios {
  usuarios: UsuarioAdministrado[] = [];
  error = '';
  private readonly inventario = inject(InventarioService);
  private readonly errorTemporal = new ErrorTemporal();

  constructor() {
    afterNextRender(() => this.cargar());
  }

  cargar(): void {
    this.inventario.usuarios().subscribe({
      next: (usuarios) => (this.usuarios = usuarios),
      error: () => this.errorTemporal.mostrar('Solo un administrador puede gestionar usuarios.', (valor) => (this.error = valor)),
    });
  }

  actualizar(usuario: UsuarioAdministrado): void {
    this.inventario.actualizarUsuario(usuario.id, { rol: usuario.rol, activo: usuario.activo }).subscribe({
      error: () => { this.errorTemporal.mostrar('No se pudo actualizar el usuario.', (valor) => (this.error = valor)); this.cargar(); },
    });
  }
}
