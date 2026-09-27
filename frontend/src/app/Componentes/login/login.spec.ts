import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { SesionService } from '../../Servicios/sesion.service';
import { AUTH_LOGIN_URL, AUTH_REGISTER_URL } from '../../Servicios/autenticacion.service';
import { Login } from './login';

describe('Login', () => {
  let component: Login;
  let fixture: ComponentFixture<Login>;
  let httpTestingController: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [
        provideRouter([{ path: 'inventario', component: Login }]),
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    }).compileComponents();

    httpTestingController = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render empty login fields without browser autofill hints', () => {
    const form = fixture.nativeElement.querySelector('form') as HTMLFormElement;
    const username = fixture.nativeElement.querySelector('#username') as HTMLInputElement;
    const password = fixture.nativeElement.querySelector('#password') as HTMLInputElement;

    expect(form.getAttribute('autocomplete')).toBe('off');
    expect(username.value).toBe('');
    expect(username.getAttribute('autocomplete')).toBe('off');
    expect(password.value).toBe('');
    expect(password.getAttribute('autocomplete')).toBe('new-password');
  });

  it('should navigate to inventory with valid demo credentials', async () => {
    const submission = component.onSubmit(new Event('submit'), 'admin', '1234', '');
    const request = httpTestingController.expectOne(AUTH_LOGIN_URL);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ usuario: 'admin', contrasena: '1234' });
    request.flush({ nombre: 'Administrador' });
    await submission;

    expect(TestBed.inject(Router).url).toBe('/inventario');
    expect(component.errorMessage).toBe('');
    expect(TestBed.inject(SesionService).nombreUsuario()).toBe('Administrador');
  });

  it('should accept the student demo credentials', async () => {
    const submission = component.onSubmit(new Event('submit'), 'estudiante', '1234', '');
    const request = httpTestingController.expectOne(AUTH_LOGIN_URL);
    request.flush({ nombre: 'Estudiante' });
    await submission;

    expect(TestBed.inject(Router).url).toBe('/inventario');
    expect(TestBed.inject(SesionService).nombreUsuario()).toBe('Estudiante');
  });

  it('should show an error when credentials are invalid', async () => {
    const submission = component.onSubmit(new Event('submit'), 'admin', 'wrong', '');
    const request = httpTestingController.expectOne(AUTH_LOGIN_URL);
    request.flush({}, { status: 401, statusText: 'Unauthorized' });
    await submission;

    expect(component.errorMessage).toBe('Usuario o contraseña incorrectos.');
    expect(TestBed.inject(Router).url).toBe('/');
  });

  it('should show invalid credentials for a forbidden response and stop loading', async () => {
    const submission = component.onSubmit(new Event('submit'), 'usuario', 'incorrecta', '');
    const request = httpTestingController.expectOne(AUTH_LOGIN_URL);
    request.flush({}, { status: 403, statusText: 'Forbidden' });
    await submission;

    expect(component.errorMessage).toBe('Usuario o contraseña incorrectos.');
    expect(component.isSubmitting).toBe(false);
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('[role="alert"]')?.textContent).toContain(
      'Usuario o contraseña incorrectos.',
    );
  });

  it('should show a connection error when the backend is unavailable', async () => {
    const submission = component.onSubmit(new Event('submit'), 'admin', '1234', '');
    const request = httpTestingController.expectOne(AUTH_LOGIN_URL);
    request.error(new ProgressEvent('error'));
    await submission;

    expect(component.errorMessage).toBe('No se pudo conectar con el servidor.');
  });

  it('should register a new user and navigate to inventory', async () => {
    component.toggleMode();
    const submission = component.onSubmit(
      new Event('submit'),
      ' nuevo_usuario ',
      'clave-segura-2026',
      ' Nombre Nuevo ',
    );
    const request = httpTestingController.expectOne(AUTH_REGISTER_URL);
    expect(request.request.body).toEqual({
      usuario: 'nuevo_usuario',
      nombre: 'Nombre Nuevo',
      contrasena: 'clave-segura-2026',
    });
    request.flush({ nombre: 'Nombre Nuevo' }, { status: 201, statusText: 'Created' });
    await submission;

    expect(TestBed.inject(Router).url).toBe('/inventario');
    expect(TestBed.inject(SesionService).nombreUsuario()).toBe('Nombre Nuevo');
  });

  it('should show an error when the requested username is already registered', async () => {
    component.toggleMode();
    const submission = component.onSubmit(
      new Event('submit'),
      'admin',
      'clave-segura-2026',
      'Otra cuenta',
    );
    const request = httpTestingController.expectOne(AUTH_REGISTER_URL);
    request.flush({}, { status: 409, statusText: 'Conflict' });
    await submission;

    expect(component.errorMessage).toBe('Ese nombre de usuario ya está registrado.');
  });
});
