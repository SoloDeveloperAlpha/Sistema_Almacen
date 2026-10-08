import { afterNextRender, Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { SesionService } from '../../Servicios/sesion.service';
import { AutenticacionService } from '../../Servicios/autenticacion.service';
import { InventarioService } from '../../Servicios/inventario.service';

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
  private readonly inventario = inject(InventarioService);
  private readonly router = inject(Router);

  constructor() {
    afterNextRender(() => this.sesion.restaurarUsuario());
  }

  cerrarSesion(event?: Event): void {
    event?.preventDefault();
    const token = this.sesion.token();
    if (token) this.autenticacion.cerrarSesion(token).subscribe({ error: () => undefined });
    this.inventario.invalidarInicial();
    this.sesion.cerrarSesion();
    this.menuAbierto.set(false);
    void this.router.navigateByUrl('/login', { replaceUrl: true }).catch(() => undefined);
  }

  alternarMenu(): void {
    this.menuAbierto.update((abierto) => !abierto);
  }

  cerrarMenu(): void {
    this.menuAbierto.set(false);
  }
}
