import { Injectable, PLATFORM_ID, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Injectable({ providedIn: 'root' })
export class SesionService {
  readonly nombreUsuario = signal('Usuario');
  readonly usuario = signal('');
  readonly rol = signal<'ADMINISTRADOR' | 'OPERATIVO'>('OPERATIVO');
  readonly token = signal('');

  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

  constructor() {
    this.restaurarUsuario();
  }

  establecerUsuario(nombre: string, usuario = nombre, rol: 'ADMINISTRADOR' | 'OPERATIVO' = 'OPERATIVO', token = ''): void {
    this.nombreUsuario.set(nombre);
    this.usuario.set(usuario);
    this.rol.set(rol);
    this.token.set(token);
    if (this.isBrowser) {
      sessionStorage.setItem('usuarioActivo', nombre);
      sessionStorage.setItem('usuarioLogin', usuario);
      sessionStorage.setItem('usuarioRol', rol);
      sessionStorage.setItem('usuarioToken', token);
    }
  }

  restaurarUsuario(): void {
    if (this.isBrowser) {
      this.nombreUsuario.set(sessionStorage.getItem('usuarioActivo') ?? 'Usuario');
      this.usuario.set(sessionStorage.getItem('usuarioLogin') ?? '');
      this.rol.set((sessionStorage.getItem('usuarioRol') as 'ADMINISTRADOR' | 'OPERATIVO') ?? 'OPERATIVO');
      this.token.set(sessionStorage.getItem('usuarioToken') ?? '');
    }
  }

  cerrarSesion(): void {
    this.nombreUsuario.set('Usuario');
    this.usuario.set('');
    this.rol.set('OPERATIVO');
    this.token.set('');
    if (this.isBrowser) {
      sessionStorage.removeItem('usuarioActivo');
      sessionStorage.removeItem('usuarioLogin');
      sessionStorage.removeItem('usuarioRol');
      sessionStorage.removeItem('usuarioToken');
    }
  }
}
