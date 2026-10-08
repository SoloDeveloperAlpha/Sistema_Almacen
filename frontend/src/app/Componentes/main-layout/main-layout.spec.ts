import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { SesionService } from '../../Servicios/sesion.service';
import { MainLayout } from './main-layout';

describe('MainLayout', () => {
  let component: MainLayout;
  let fixture: ComponentFixture<MainLayout>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MainLayout],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(MainLayout);
    component = fixture.componentInstance;
    await fixture.whenStable();
    fixture.detectChanges();
  });

  it('should display the signed-in user and clear it on logout', () => {
    const session = TestBed.inject(SesionService);
    const username = fixture.nativeElement.querySelector('.btn-primary span') as HTMLElement;

    session.establecerUsuario('Walter');
    fixture.detectChanges();
    expect(username.textContent?.trim()).toBe('Walter');

    component.cerrarSesion();
    fixture.detectChanges();
    expect(username.textContent?.trim()).toBe('Usuario');
  });

  it('should open the responsive navigation with all inventory options', () => {
    const toggle = fixture.nativeElement.querySelector('.navbar-toggler') as HTMLButtonElement;
    const navigation = fixture.nativeElement.querySelector('#navbarStockFlow') as HTMLElement;

    toggle.click();
    fixture.detectChanges();

    expect(toggle.getAttribute('aria-expanded')).toBe('true');
    expect(navigation.classList.contains('show')).toBe(true);
    expect(navigation.textContent).toContain('Inventario');
    expect(navigation.textContent).toContain('Registrar Entrada');
    expect(navigation.textContent).toContain('Registrar Salida');
    expect(navigation.textContent).toContain('Historial');
  });

  it('should close the responsive navigation after choosing an option', () => {
    component.alternarMenu();
    expect(component.menuAbierto()).toBe(true);

    component.cerrarMenu();
    fixture.detectChanges();

    expect(component.menuAbierto()).toBe(false);
    expect(fixture.nativeElement.querySelector('#navbarStockFlow').classList.contains('show')).toBe(false);
  });
});
