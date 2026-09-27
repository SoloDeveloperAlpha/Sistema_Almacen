import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Inventario } from './inventario';

describe('Inventario', () => {
  let component: Inventario;
  let fixture: ComponentFixture<Inventario>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Inventario],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Inventario);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
