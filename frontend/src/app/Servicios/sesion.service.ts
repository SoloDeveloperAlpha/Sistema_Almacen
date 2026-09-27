import { Injectable, PLATFORM_ID, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Injectable({ providedIn: 'root' })
export class SesionService {
  readonly nombreUsuario = signal('Usuario');

  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

  establecerUsuario(nombre: string): void {
    this.nombreUsuario.set(nombre);
    if (this.isBrowser) {
      sessionStorage.setItem('usuarioActivo', nombre);
    }
  }

  restaurarUsuario(): void {
    if (this.isBrowser) {
      this.nombreUsuario.set(sessionStorage.getItem('usuarioActivo') ?? 'Usuario');
    }
  }

  cerrarSesion(): void {
    this.nombreUsuario.set('Usuario');
    if (this.isBrowser) {
      sessionStorage.removeItem('usuarioActivo');
    }
  }
}
