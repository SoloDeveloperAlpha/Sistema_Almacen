export const ERROR_VISIBLE_MS = 8000;

export class ErrorTemporal {
  private timer: ReturnType<typeof setTimeout> | undefined;

  // "notificar" permite avisar al detector de cambios cuando el mensaje se oculta
  // solo mediante setTimeout (fuera de cualquier evento del DOM u observable).
  mostrar(mensaje: string, asignar: (valor: string) => void, notificar?: () => void): void {
    this.limpiar(asignar);
    asignar(mensaje);
    this.timer = setTimeout(() => {
      asignar('');
      this.timer = undefined;
      notificar?.();
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
