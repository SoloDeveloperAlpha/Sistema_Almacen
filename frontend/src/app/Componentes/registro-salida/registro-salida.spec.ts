import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { provideRouter } from '@angular/router';
import { RegistroSalida } from './registro-salida';

describe('RegistroSalida', () => {
  let component: RegistroSalida;
  let fixture: ComponentFixture<RegistroSalida>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegistroSalida],
      providers: [
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { queryParamMap: { get: () => null } } } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RegistroSalida);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
