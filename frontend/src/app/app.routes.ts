import { Routes } from '@angular/router';
import { Historial } from './Componentes/historial/historial';
import { Inventario } from './Componentes/inventario/inventario';
import { Login } from './Componentes/login/login';
import { MainLayout } from './Componentes/main-layout/main-layout';
import { RegistroEntrada } from './Componentes/registro-entrada/registro-entrada';
import { RegistroSalida } from './Componentes/registro-salida/registro-salida';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: Login },
  {
    path: '',
    component: MainLayout,
    children: [
      { path: '', redirectTo: 'inventario', pathMatch: 'full' },
      { path: 'inventario', component: Inventario },
      { path: 'registro-entrada', component: RegistroEntrada },
      { path: 'registro-salida', component: RegistroSalida },
      { path: 'historial', component: Historial },
    ],
  },
  { path: '**', redirectTo: 'login' },
];
