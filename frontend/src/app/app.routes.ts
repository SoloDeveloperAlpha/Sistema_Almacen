import { Routes } from '@angular/router';
import { Login } from './Componentes/login/login';
import { MainLayout } from './Componentes/main-layout/main-layout';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: Login },
  {
    path: '',
    component: MainLayout,
    children: [
      { path: '', redirectTo: 'inventario', pathMatch: 'full' },
      { path: 'inventario', loadComponent: () => import('./Componentes/inventario/inventario').then((m) => m.Inventario) },
      { path: 'registro-entrada', loadComponent: () => import('./Componentes/registro-entrada/registro-entrada').then((m) => m.RegistroEntrada) },
      { path: 'registro-salida', loadComponent: () => import('./Componentes/registro-salida/registro-salida').then((m) => m.RegistroSalida) },
      { path: 'historial', loadComponent: () => import('./Componentes/historial/historial').then((m) => m.Historial) },
      { path: 'usuarios', loadComponent: () => import('./Componentes/usuarios/usuarios').then((m) => m.Usuarios) },
    ],
  },
  { path: '**', redirectTo: 'login' },
];
