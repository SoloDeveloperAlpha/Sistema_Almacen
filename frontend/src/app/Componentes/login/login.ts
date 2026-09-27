import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { firstValueFrom, TimeoutError } from 'rxjs';
import { AutenticacionService } from '../../Servicios/autenticacion.service';
import { SesionService } from '../../Servicios/sesion.service';
import { ErrorTemporal } from '../../Servicios/error-temporal';

@Component({
  imports: [],
  selector: 'app-login',
  styleUrl: './login.css',
  templateUrl: './login.html',
})
export class Login {
  errorMessage = '';
  isSubmitting = false;
  isRegisterMode = false;

  private readonly router = inject(Router);
  private readonly changeDetector = inject(ChangeDetectorRef);
  private readonly autenticacion = inject(AutenticacionService);
  private readonly sesion = inject(SesionService);
  private readonly errorTemporal = new ErrorTemporal();

  private mostrarError(mensaje: string): void {
    this.errorTemporal.mostrar(mensaje, (valor) => (this.errorMessage = valor));
  }

  toggleMode(): void {
    this.isRegisterMode = !this.isRegisterMode;
    this.errorTemporal.limpiar((valor) => (this.errorMessage = valor));
  }

  async onSubmit(
    event: Event,
    usernameValue: string,
    passwordValue: string,
    nameValue: string,
  ): Promise<void> {
    event.preventDefault();
    this.errorTemporal.limpiar((valor) => (this.errorMessage = valor));
    this.isSubmitting = true;

    try {
      const respuesta = await firstValueFrom(
        this.isRegisterMode
          ? this.autenticacion.registrar(usernameValue.trim(), nameValue.trim(), passwordValue)
          : this.autenticacion.autenticar(usernameValue.trim(), passwordValue),
      );
      this.sesion.establecerUsuario(respuesta.nombre, respuesta.usuario, respuesta.rol, respuesta.token);
      await this.router.navigateByUrl('/inventario');
    } catch (error) {
      if (error instanceof TimeoutError) {
        this.mostrarError('El servidor no respondió a tiempo. Revisa Spring Boot y MySQL.');
      } else if (
        error instanceof HttpErrorResponse &&
        (error.status === 401 || error.status === 403)
      ) {
        this.mostrarError('Usuario o contraseña incorrectos.');
      } else if (error instanceof HttpErrorResponse && error.status === 409) {
        this.mostrarError('Ese nombre de usuario ya está registrado.');
      } else if (error instanceof HttpErrorResponse && error.status === 400) {
        this.mostrarError(this.isRegisterMode
          ? 'Revisa los datos: la contraseña debe tener al menos 8 caracteres.'
          : 'Completa el usuario y la contraseña.');
      } else if (error instanceof HttpErrorResponse && error.status === 0) {
        this.mostrarError('No se pudo conectar con el servidor.');
      } else if (error instanceof HttpErrorResponse && error.status >= 500) {
        this.mostrarError('El servidor encontró un problema. Revisa la conexión con MySQL y los logs de Spring Boot.');
      } else if (error instanceof HttpErrorResponse && error.status === 404) {
        this.mostrarError('La API de autenticación no está disponible en el puerto 8080.');
      } else {
        this.mostrarError('No se pudo iniciar sesión. Inténtalo nuevamente.');
      }
    } finally {
      this.isSubmitting = false;
      this.changeDetector.markForCheck();
    }
  }
}
