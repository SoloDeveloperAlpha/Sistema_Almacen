import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { firstValueFrom, TimeoutError } from 'rxjs';
import { AutenticacionService } from '../../Servicios/autenticacion.service';
import { SesionService } from '../../Servicios/sesion.service';

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

  toggleMode(): void {
    this.isRegisterMode = !this.isRegisterMode;
    this.errorMessage = '';
  }

  async onSubmit(
    event: Event,
    usernameValue: string,
    passwordValue: string,
    nameValue: string,
  ): Promise<void> {
    event.preventDefault();
    this.errorMessage = '';
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
        this.errorMessage = 'El servidor no respondió a tiempo. Revisa Spring Boot y MySQL.';
      } else if (
        error instanceof HttpErrorResponse &&
        (error.status === 401 || error.status === 403)
      ) {
        this.errorMessage = 'Usuario o contraseña incorrectos.';
      } else if (error instanceof HttpErrorResponse && error.status === 409) {
        this.errorMessage = 'Ese nombre de usuario ya está registrado.';
      } else if (error instanceof HttpErrorResponse && error.status === 400) {
        this.errorMessage = this.isRegisterMode
          ? 'Revisa los datos: la contraseña debe tener al menos 8 caracteres.'
          : 'Completa el usuario y la contraseña.';
      } else if (error instanceof HttpErrorResponse && error.status === 0) {
        this.errorMessage = 'No se pudo conectar con el servidor.';
      } else {
        this.errorMessage = 'No se pudo iniciar sesión. Inténtalo nuevamente.';
      }
    } finally {
      this.isSubmitting = false;
      this.changeDetector.markForCheck();
    }
  }
}
