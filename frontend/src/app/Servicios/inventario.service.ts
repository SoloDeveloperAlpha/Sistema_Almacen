import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { SesionService } from './sesion.service';

export interface Producto {
  id: number;
  codigo: string;
  nombre: string;
  categoria: string;
  unidadMedida: string;
  stockActual: number;
  stockMinimo: number;
  ubicacion?: string;
  proveedor?: string;
}

export interface Movimiento {
  id: number;
  fecha: string;
  tipo: 'ENTRADA' | 'SALIDA';
  cantidad: number;
  motivo?: string;
  tercero?: string;
  productoId: number;
  productoCodigo: string;
  productoNombre: string;
  usuario: string;
}

export interface ResumenInventario {
  productos: number;
  unidades: number;
  stockBajo: number;
  agotados: number;
  movimientos: number;
}

export interface UsuarioAdministrado {
  id: number;
  usuario: string;
  nombre: string;
  rol: 'ADMINISTRADOR' | 'OPERATIVO';
  activo: boolean;
}

const API = 'http://localhost:8080/api';

@Injectable({ providedIn: 'root' })
export class InventarioService {
  private readonly http = inject(HttpClient);
  private readonly sesion = inject(SesionService);

  private headers() {
    return { Authorization: `Bearer ${this.sesion.token()}` };
  }

  productos(buscar = '', categoria = ''): Observable<Producto[]> {
    const params = new HttpParams({ fromObject: { buscar, categoria } });
    return this.http.get<Producto[]>(`${API}/productos`, { params, headers: this.headers() });
  }

  categorias(): Observable<string[]> {
    return this.http.get<string[]>(`${API}/productos/categorias`, { headers: this.headers() });
  }

  resumen(): Observable<ResumenInventario> {
    return this.http.get<ResumenInventario>(`${API}/reportes/resumen`, { headers: this.headers() });
  }

  reporteInventario(): Observable<Blob> {
    return this.http.get(`${API}/reportes/inventario.csv`, { headers: this.headers(), responseType: 'blob' });
  }

  reporteMovimientos(filtros: { tipo?: string; desde?: string; hasta?: string; buscar?: string } = {}): Observable<Blob> {
    const params = new HttpParams({ fromObject: filtros as Record<string, string> });
    return this.http.get(`${API}/reportes/movimientos.csv`, { params, headers: this.headers(), responseType: 'blob' });
  }

  movimientos(filtros: { tipo?: string; desde?: string; hasta?: string; buscar?: string } = {}): Observable<Movimiento[]> {
    const params = new HttpParams({ fromObject: filtros as Record<string, string> });
    return this.http.get<Movimiento[]>(`${API}/movimientos`, { params, headers: this.headers() });
  }

  registrarEntrada(datos: object): Observable<Movimiento> {
    return this.http.post<Movimiento>(`${API}/movimientos/entrada`, datos, { headers: this.headers() });
  }

  registrarSalida(datos: object): Observable<Movimiento> {
    return this.http.post<Movimiento>(`${API}/movimientos/salida`, datos, { headers: this.headers() });
  }

  usuarios(): Observable<UsuarioAdministrado[]> {
    return this.http.get<UsuarioAdministrado[]>(`${API}/usuarios`, { headers: this.headers() });
  }

  actualizarUsuario(id: number, datos: Partial<UsuarioAdministrado>): Observable<UsuarioAdministrado> {
    return this.http.patch<UsuarioAdministrado>(`${API}/usuarios/${id}`, datos, { headers: this.headers() });
  }
}
