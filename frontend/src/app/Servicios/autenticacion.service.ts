import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, timeout } from 'rxjs';

export interface RespuestaLogin {
  nombre: string;
}

export const AUTH_LOGIN_URL = 'http://localhost:8080/api/auth/login';
export const AUTH_REGISTER_URL = 'http://localhost:8080/api/auth/registro';
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
}
