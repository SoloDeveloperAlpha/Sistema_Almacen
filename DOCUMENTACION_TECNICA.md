# Documentacion tecnica: Sistema Almace

Este documento describe la arquitectura y la API que existen actualmente en el repositorio, junto con practicas observadas, pendientes tecnicos y una ruta propuesta para desplegar el sistema. Los puntos marcados como mejora son recomendaciones; no representan funcionalidades ya implementadas.

## 1. Resumen del sistema

Aplicacion web para administrar productos, existencias, entradas y salidas de inventario, usuarios y reportes. El repositorio contiene:

- **Frontend:** Angular 22 y TypeScript. Se sirve en desarrollo con Angular CLI y tiene configurados renderizado del lado del servidor (SSR) y prerenderizado.
- **Backend:** Spring Boot 4.1.1 sobre Java 25. Expone una API REST y se empaqueta como WAR.
- **Persistencia:** MySQL para desarrollo/ejecucion normal y H2 en memoria para las pruebas del backend.

## 2. Arquitectura

```text
Navegador
  └── Angular: rutas y componentes
       └── servicios HTTP ── JSON / CSV ──> Spring Boot REST API
                                             └── servicios de dominio
                                                  └── Spring Data JPA / Hibernate
                                                       └── MySQL

SSR de Angular: Node.js + Express + Angular SSR (servidor configurado por defecto en puerto 4000)
Desarrollo: Angular CLI en puerto 4200; API Spring Boot en puerto 8080
```

### Capas y ubicaciones

| Capa | Responsabilidad | Ubicacion representativa |
|---|---|---|
| Interfaz | Componentes Angular para inventario, entradas, salidas, historial, inicio de sesion y usuarios; rutas con carga diferida. | `frontend/src/app/Componentes/`, `frontend/src/app/app.routes.ts` |
| Cliente API | Tipos de respuesta, solicitudes HTTP y almacenamiento del estado de sesion. | `frontend/src/app/Servicios/` |
| Controladores | Rutas REST, lectura de parametros y respuestas HTTP. | `backend/src/main/java/com/almacen/stock_flow/*Controller.java` |
| Servicios | Reglas del inventario, autenticacion y gestion de usuarios. | `InventarioService.java`, `UsuarioService.java` |
| Repositorios | Acceso a entidades mediante Spring Data JPA. | `*Repository.java` |
| Modelo | Entidades JPA y enumeraciones del dominio. | `Producto.java`, `Movimiento.java`, `Usuario.java`, `Rol.java`, `TipoMovimiento.java` |
| Configuracion y arranque | Configuracion de CORS/seguridad, propiedades y punto de entrada Spring Boot. | `SecurityConfig.java`, `application.properties`, `StockFlowApplication.java` |

Las entidades principales son `Producto`, `Movimiento` y `Usuario`. Cada movimiento se asocia a un producto y a un usuario. Los productos tienen codigo unico y los nombres de usuario son unicos. La entrada de mercancia agrega stock a un producto activo existente cuando coinciden nombre, categoria y proveedor; de lo contrario, crea un producto y genera su SKU.

El frontend tiene SSR configurado en `angular.json`; `backend/server.ts` ejecuta Angular SSR con Express y usa `PORT` (por defecto, `4000`). La configuracion de rutas del servidor declara `RenderMode.Prerender` para `**`. La API Spring Boot es un proceso separado.

## 3. API REST

### Convenciones

- URL local base: `http://localhost:8080/api`.
- Las solicitudes y respuestas de datos usan JSON, salvo los reportes CSV.
- Salvo inicio de sesion y registro, los endpoints requieren `Authorization: Bearer <token>`.
- El token es una sesion opaca generada y guardada por el backend; **no es un JWT**. Caduca por defecto a las 8 horas (`SESSION_DURATION_HOURS`).
- Los parametros `desde` y `hasta` usan fecha ISO `YYYY-MM-DD`; `tipo` admite `ENTRADA` o `SALIDA`.
- `ADMINISTRADOR` es necesario para consultar o actualizar cuentas. Las operaciones de inventario requieren una sesion activa.

### Endpoints

| Metodo y ruta | Acceso | Parametros / cuerpo | Respuesta |
|---|---|---|---|
| `POST /auth/login` | Publico | `{ "usuario": "...", "contrasena": "..." }` | `200`: usuario, nombre, rol y token |
| `POST /auth/registro` | Publico | `{ "usuario": "...", "nombre": "...", "contrasena": "..." }` | `201`: usuario, nombre, rol y token |
| `POST /auth/logout` | Sesion activa | Sin cuerpo; token en `Authorization` | `204` |
| `GET /productos` | Sesion activa | Opcionales: `buscar`, `categoria` | `200`: productos activos |
| `GET /productos/categorias` | Sesion activa | Ninguno | `200`: categorias |
| `GET /inventario/inicial` | Sesion activa | Ninguno | `200`: productos, categorias y resumen |
| `POST /movimientos/entrada` | Sesion activa | Campos descritos abajo | `201`: movimiento registrado |
| `POST /movimientos/salida` | Sesion activa | Campos descritos abajo | `201`: movimiento registrado |
| `GET /movimientos` | Sesion activa | Opcionales: `tipo`, `desde`, `hasta`, `buscar` | `200`: movimientos filtrados |
| `GET /reportes/resumen` | Sesion activa | Ninguno | `200`: contadores de inventario |
| `GET /reportes/inventario.csv` | Sesion activa | Ninguno | `200`: descarga CSV |
| `GET /reportes/movimientos.csv` | Sesion activa | Opcionales: `tipo`, `desde`, `hasta`, `buscar` | `200`: descarga CSV filtrada |
| `GET /usuarios` | Administrador | Ninguno | `200`: usuarios sin contrasenas |
| `PATCH /usuarios/{id}` | Administrador | Uno o mas campos opcionales: `rol`, `activo`, `nombre` | `200`: usuario actualizado |

#### Cuerpos para movimientos

`POST /movimientos/entrada`:

```json
{
  "nombre": "Teclado",
  "categoria": "Perifericos",
  "unidadMedida": "unidad",
  "cantidad": 12,
  "stockMinimo": 3,
  "ubicacion": "Estante A",
  "proveedor": "Proveedor Uno",
  "observaciones": "Recepcion inicial"
}
```

`nombre`, `categoria`, `unidadMedida` y `proveedor` son obligatorios; `cantidad` debe ser al menos 1 y `stockMinimo` no puede ser negativo.

`POST /movimientos/salida`:

```json
{
  "productoId": 1,
  "cantidad": 2,
  "motivo": "Entrega",
  "destino": "Area de soporte",
  "observaciones": ""
}
```

`productoId`, `cantidad` y `motivo` son obligatorios. El servidor rechaza la operacion si no hay stock suficiente.

### Validaciones y errores habituales

- El registro valida el nombre de usuario (3-32 caracteres, letras ASCII, numeros, punto, guion o guion bajo) y una contrasena de 8-72 caracteres.
- `400`: solicitud invalida, validacion fallida o tipo de movimiento desconocido.
- `401`: credenciales invalidas, sesion ausente, revocada o vencida.
- `403`: un usuario operativo intenta una operacion administrativa.
- `404`: recurso solicitado no encontrado.
- `409`: usuario duplicado o stock insuficiente.

## 4. Buenas practicas y estandares

### Practicas ya presentes

- Separacion de controladores, servicios, repositorios y entidades.
- Inyeccion de dependencias por constructor en el backend.
- Validacion declarativa de varios cuerpos mediante `@Valid` y Bean Validation.
- Uso de transacciones en operaciones que modifican inventario o usuarios.
- Contraseñas almacenadas con BCrypt; las respuestas de usuario omiten el hash.
- Restricciones e indices en algunas columnas/consultas frecuentes y relaciones explicitas entre entidades.
- Configuracion operativa de Spring mediante variables de entorno, CORS con origen configurable y Maven Wrapper.
- H2 aisla las pruebas del backend de MySQL. Hay pruebas de autenticacion, movimientos, control de stock, sesion y generacion de codigos; tambien existen pruebas unitarias de componentes Angular.
- TypeScript se usa con interfaces para representar respuestas de la API y con opciones de compilador como `noImplicitReturns`; Angular configura comprobaciones estrictas para inyeccion y accesos a entradas.
- Convenciones visibles: paquetes Java en minusculas, clases Java en PascalCase, metodos/variables Java en camelCase y componentes Angular organizados por funcionalidad.

### Criterios recomendados para nuevas contribuciones

1. Mantener una responsabilidad clara por capa; poner reglas de negocio en servicios, no en controladores ni componentes.
2. Validar toda entrada en el servidor, no confiar en validaciones de formulario del navegador.
3. Usar DTOs para entrada y salida de la API; no exponer entidades JPA directamente.
4. Escribir pruebas para reglas de negocio, autorizacion, errores y regresiones, ademas de los casos felices.
5. Usar nombres explicitos, tipos concretos, metodos pequenos y evitar duplicar reglas entre frontend y backend.
6. Mantener secretos fuera del repositorio y documentar las variables de entorno requeridas.
7. Ejecutar compilacion y pruebas de frontend/backend antes de integrar cambios; incorporar formato y analisis estatico automatizados.

## 5. Observaciones y mejoras pendientes

Priorizar antes de poner el sistema a disposicion de usuarios externos:

1. **Configurar la URL de API por entorno.** Los servicios Angular contienen `http://localhost:8080/api` directamente. En produccion el navegador intentaria conectar con su propio `localhost`. Introducir una configuracion para desarrollo/staging/produccion, incluyendo el valor utilizado durante SSR.
2. **Endurecer la autorizacion HTTP.** `SecurityConfig` permite las solicitudes a `/api/**` a nivel de filtro y los controladores/servicios verifican manualmente el token. Centralizar la autenticacion/autorizacion en Spring Security y declarar expresamente rutas publicas, autenticadas y administrativas; cubrirlo con pruebas.
3. **Revisar almacenamiento de sesion.** El token se guarda en `sessionStorage`, accesible desde JavaScript. Evaluar una cookie `HttpOnly`, `Secure` y `SameSite` junto con proteccion CSRF, o documentar y reforzar el modelo actual contra XSS.
4. **Gestionar cambios de esquema.** `spring.jpa.hibernate.ddl-auto=update` modifica el esquema al iniciar. Para produccion, incorporar migraciones versionadas (Flyway/Liquibase), revisar primero el esquema existente y definir respaldo/restauracion.
5. **Evitar cargar todos los registros en memoria.** Algunas busquedas recorren `findAll()` y filtran en Java. Tras definir volumen esperado, mover filtros/orden a consultas SQL, incorporar paginacion y medir indices.
6. **Normalizar contratos y errores.** Publicar OpenAPI, usar DTOs para productos y respuestas consistentes de error (por ejemplo, `ProblemDetail`), y validar los parametros opcionales y combinaciones de fechas.
7. **Proteger consistencia ante concurrencia.** Revisar operaciones simultaneas sobre stock y asignacion del consecutivo SKU con restricciones/locking de base de datos y pruebas concurrentes; `synchronized` solo coordina dentro de una instancia JVM.
8. **Preparar operacion.** Definir comprobaciones de salud/readiness, logs estructurados y metricas, rotacion de secretos, politica de respaldos y retencion, y alertas. Establecer en CI verificaciones automatizadas de compilacion, pruebas y dependencias.
9. **Fijar estandares de calidad.** No hay un comando de lint/analisis estatico del proyecto documentado en `package.json`; definir reglas y ejecutar formatter/linter Java y TypeScript desde CI.
10. **Probar SSR en entorno real.** Validar prerenderizado, hidratacion y rutas que dependen de sesion; asegurar que la salida generada no muestre datos privados y que el servidor Node tenga un ciclo de vida administrado.

## 6. Plan propuesto de despliegue

### Fase 1: preparar configuracion y servicios

- Elegir dominio, regiones y si frontend/API compartiran origen. Se recomienda un proxy inverso HTTPS con el frontend y una ruta `/api` hacia Spring Boot para simplificar CORS.
- Resolver primero la configuracion de URL de API del frontend (mejora 1); no desplegar la configuracion actual esperando que `localhost:8080` apunte al backend remoto.
- Provisionar MySQL administrado o servidor MySQL con respaldo automatizado, acceso de red restringido y una base creada previamente. Crear un usuario de aplicacion con privilegios minimos; evitar `root`.
- Guardar secretos en el gestor del entorno de despliegue, no en archivos versionados.

### Fase 2: construir y verificar artefactos

Desde la raiz del repositorio, ejecutar en CI o en un entorno de compilacion limpio:

```powershell
npm ci
npm test -- --watch=false
npm run build
.\backend\mvnw.cmd -f backend\pom.xml clean verify
```

Ejecutar ademas una prueba de humo del frontend SSR y una verificacion de la API contra una base de staging. Conservar los artefactos asociados al commit y no compilar de nuevo en el servidor de produccion.

### Fase 3: desplegar backend y frontend

- Ejecutar el WAR de Spring Boot con Java 25, por ejemplo `java -jar backend/target/stock_flow-0.0.1-SNAPSHOT.war`, o desplegarlo en un contenedor servlet compatible. Proveer `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `FRONTEND_ORIGINS` y `SESSION_DURATION_HOURS` desde el entorno.
- Establecer `BOOTSTRAP_ADMIN_USERNAME` solo durante la provision inicial del administrador y retirar esa variable al terminar el alta inicial.
- Desplegar la salida SSR de Angular en un runtime Node.js compatible con Angular. El script existente es `npm run serve:ssr:Sistema_Almace`; el servidor escucha en `PORT` o, si no se configura, en el puerto `4000`.
- Configurar el proxy inverso para terminar TLS, enviar `/api` al backend y el resto de rutas al servidor Angular. Permitir en CORS solo los origenes necesarios; no usar `*` como sustituto de una configuracion de produccion.

### Fase 4: validar, observar y mantener

- Comprobar login, registro, rol administrador, lectura de inventario, entrada, salida con stock insuficiente, filtros, CSV y cierre de sesion.
- Verificar que los tokens vencidos/inactivos no permiten operaciones y que las rutas administrativas responden correctamente por rol.
- Confirmar logs, readiness de base de datos, metricas, alertas y respaldo/restauracion antes de abrir el acceso.
- Documentar version desplegada, variables requeridas, procedimiento de rollback y pasos para aplicar/restaurar migraciones.

## 7. Ejecucion y pruebas locales

Los comandos de instalacion y ejecucion en Windows/macOS/Linux estan en [README.md](README.md). Para pruebas se usan:

```bash
npm test -- --watch=false
./backend/mvnw -f backend/pom.xml test
```

En Windows, usar `.\backend\mvnw.cmd -f backend\pom.xml test` para el backend. Las pruebas del backend usan H2 en memoria; no requieren una instancia MySQL.
