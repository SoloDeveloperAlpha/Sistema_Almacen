import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { RegistroEntrada } from './registro-entrada';

describe('RegistroEntrada', () => {
  let component: RegistroEntrada;
  let fixture: ComponentFixture<RegistroEntrada>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegistroEntrada],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(RegistroEntrada);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
