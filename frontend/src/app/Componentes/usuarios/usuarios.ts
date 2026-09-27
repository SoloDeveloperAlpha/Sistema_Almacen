import { afterNextRender, Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { InventarioService, UsuarioAdministrado } from '../../Servicios/inventario.service';

@Component({
  imports: [FormsModule],
  selector: 'app-usuarios',
  templateUrl: './usuarios.html',
})
export class Usuarios {
  usuarios: UsuarioAdministrado[] = [];
  error = '';
  private readonly inventario = inject(InventarioService);

  constructor() {
    afterNextRender(() => this.cargar());
  }

  cargar(): void {
    this.inventario.usuarios().subscribe({
      next: (usuarios) => (this.usuarios = usuarios),
      error: () => (this.error = 'Solo un administrador puede gestionar usuarios.'),
    });
  }

  actualizar(usuario: UsuarioAdministrado): void {
    this.inventario.actualizarUsuario(usuario.id, { rol: usuario.rol, activo: usuario.activo }).subscribe({
      error: () => { this.error = 'No se pudo actualizar el usuario.'; this.cargar(); },
    });
  }
}
