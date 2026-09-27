import { afterNextRender, Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { SesionService } from '../../Servicios/sesion.service';

@Component({
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  selector: 'app-main-layout',
  templateUrl: './main-layout.html',
})
export class MainLayout {
  readonly menuAbierto = signal(false);

  private readonly sesion = inject(SesionService);
  readonly nombreUsuario = this.sesion.nombreUsuario;

  constructor() {
    afterNextRender(() => this.sesion.restaurarUsuario());
  }

  cerrarSesion(): void {
    this.sesion.cerrarSesion();
  }

  alternarMenu(): void {
    this.menuAbierto.update((abierto) => !abierto);
  }

  cerrarMenu(): void {
    this.menuAbierto.set(false);
  }
}
