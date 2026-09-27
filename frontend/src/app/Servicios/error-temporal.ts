export const ERROR_VISIBLE_MS = 8000;

export class ErrorTemporal {
  private timer: ReturnType<typeof setTimeout> | undefined;

  mostrar(mensaje: string, asignar: (valor: string) => void): void {
    this.limpiar(asignar);
    asignar(mensaje);
    this.timer = setTimeout(() => {
      asignar('');
      this.timer = undefined;
    }, ERROR_VISIBLE_MS);
  }

  limpiar(asignar: (valor: string) => void): void {
    if (this.timer) {
      clearTimeout(this.timer);
      this.timer = undefined;
    }
    asignar('');
  }
}
