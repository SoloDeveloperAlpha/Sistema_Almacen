import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, timeout } from 'rxjs';
import { apiBaseUrl } from './api-url';

export interface RespuestaLogin {
  usuario: string;
  nombre: string;
  rol: 'ADMINISTRADOR' | 'OPERATIVO';
  token: string;
}

const AUTH_API = `${apiBaseUrl()}/auth`;
export const AUTH_LOGIN_URL = `${AUTH_API}/login`;
export const AUTH_REGISTER_URL = `${AUTH_API}/registro`;
export const AUTH_LOGOUT_URL = `${AUTH_API}/logout`;
export const AUTH_REQUEST_TIMEOUT_MS = 10000;

@Injectable({ providedIn: 'root' })
export class AutenticacionService {
  private readonly http = inject(HttpClient);

  autenticar(usuario: string, contrasena: string): Observable<RespuestaLogin> {
    return this.http
      .post<RespuestaLogin>(AUTH_LOGIN_URL, { usuario, contrasena })
      .pipe(timeout({ first: AUTH_REQUEST_TIMEOUT_MS }));
  }

  registrar(usuario: string, nombre: string, contrasena: string): Observable<RespuestaLogin> {
    return this.http
      .post<RespuestaLogin>(AUTH_REGISTER_URL, { usuario, nombre, contrasena })
      .pipe(timeout({ first: AUTH_REQUEST_TIMEOUT_MS }));
  }

  cerrarSesion(token: string): Observable<void> {
    return this.http.post<void>(AUTH_LOGOUT_URL, {}, { headers: { Authorization: `Bearer ${token}` } });
  }
}
