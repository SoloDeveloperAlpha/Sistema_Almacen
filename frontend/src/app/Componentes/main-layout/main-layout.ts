import { afterNextRender, Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { SesionService } from '../../Servicios/sesion.service';
import { AutenticacionService } from '../../Servicios/autenticacion.service';

@Component({
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  selector: 'app-main-layout',
  templateUrl: './main-layout.html',
})
export class MainLayout {
  readonly menuAbierto = signal(false);

  private readonly sesion = inject(SesionService);
  readonly nombreUsuario = this.sesion.nombreUsuario;
  readonly sesionActual = this.sesion;
  private readonly autenticacion = inject(AutenticacionService);

  constructor() {
    afterNextRender(() => this.sesion.restaurarUsuario());
  }

  cerrarSesion(): void {
    const token = this.sesion.token();
    if (token) this.autenticacion.cerrarSesion(token).subscribe({ error: () => undefined });
    this.sesion.cerrarSesion();
  }

  alternarMenu(): void {
    this.menuAbierto.update((abierto) => !abierto);
  }

  cerrarMenu(): void {
    this.menuAbierto.set(false);
  }
}
