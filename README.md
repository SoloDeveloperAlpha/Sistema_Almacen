# Sistema Almace

Aplicacion web para gestionar el inventario, los registros de entrada y salida, y el historial de movimientos. El proyecto esta formado por:

- **Frontend:** Angular 22, servido normalmente en `http://localhost:4200`.
- **Backend:** Spring Boot 4, con API REST en `http://localhost:8080`.
- **Base de datos:** MySQL, con la base `stock_flow`.

## Requisitos

Instala lo siguiente antes de comenzar:

- [Node.js](https://nodejs.org/) compatible con npm 11.17.0.
- Java Development Kit (JDK) 25. Comprueba la version con `java -version`.
- MySQL Server en ejecucion y una cuenta con permisos para crear la base de datos.
- Git, si vas a clonar el repositorio.

El proyecto incluye Maven Wrapper, por lo que no es necesario instalar Maven por separado.

## Tecnologias utilizadas

- **Angular 22 y TypeScript:** forman la interfaz web. Angular organiza la aplicacion en componentes y servicios, y TypeScript aporta tipos que ayudan a detectar errores durante el desarrollo.
- **Angular CLI y npm:** el CLI sirve la aplicacion localmente y genera la compilacion; npm instala y administra las dependencias del frontend.
- **Spring Boot 4.1.1 y Java 25:** implementan el backend y la API REST que atiende las solicitudes del frontend. Spring Boot simplifica la configuracion y ejecucion del servidor.
- **Spring Security y Bean Validation:** permiten proteger las operaciones y validar los datos recibidos por la API.
- **Spring Data JPA e Hibernate:** relacionan las entidades Java con las tablas y gestionan la persistencia, evitando tener que escribir manualmente las operaciones habituales de base de datos.
- **MySQL:** almacena de forma persistente los usuarios, productos y movimientos del inventario.
- **Bootstrap 5, ng-bootstrap y Bootstrap Icons:** proporcionan estilos, componentes visuales e iconos para construir una interfaz adaptable.
- **Maven Wrapper:** descarga y ejecuta la version de Maven requerida sin que tengas que instalar Maven globalmente.
- **H2:** se usa como base de datos en memoria para las pruebas del backend, manteniendolas aisladas de la base MySQL de desarrollo.

## Instalacion

Clona el repositorio y entra en su carpeta:

```bash
git clone <URL_DEL_REPOSITORIO>
cd Sistema_Almace
```

Instala las dependencias del frontend desde la raiz del proyecto:

```bash
npm install
```

Inicia MySQL antes de levantar el backend. La configuracion predeterminada espera:

```text
Servidor: localhost
Puerto: 3306
Base de datos: stock_flow
Usuario: root
```

La aplicacion crea automaticamente la base `stock_flow` y las tablas `usuarios`, `productos` y `movimientos` si la cuenta configurada tiene permisos suficientes. Las relaciones guardan cada movimiento asociado al producto y al usuario que lo registro. No es necesario ejecutar un script SQL inicial.

## Ejecutar en desarrollo

Abre dos terminales en la carpeta raiz del proyecto.

### 1. Iniciar el backend

En **PowerShell** (Windows):

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "TU_CONTRASENA_DE_MYSQL"
.\backend\mvnw.cmd -f backend\pom.xml spring-boot:run
```

En **macOS o Linux**:

```bash
export DB_USERNAME=root
export DB_PASSWORD='TU_CONTRASENA_DE_MYSQL'
./backend/mvnw -f backend/pom.xml spring-boot:run
```

Si el usuario no tiene contrasena, deja `DB_PASSWORD` vacia. Para usar otra conexion, define tambien `DB_URL`, por ejemplo:

```text
DB_URL=jdbc:mysql://localhost:3306/stock_flow?createDatabaseIfNotExist=true&serverTimezone=UTC
```

Para un entorno real, usa una cuenta de MySQL dedicada a `stock_flow` en lugar de `root`.

### 2. Iniciar el frontend

En la segunda terminal, desde la raiz:

```bash
npm start
```

Abre [http://localhost:4200](http://localhost:4200) en el navegador. El frontend utiliza la API en `http://localhost:8080/api`.
Al iniciar sesión, el backend entrega un token temporal que Angular usa para autorizar las consultas y registrar quién realiza cada movimiento.
Los tokens caducan por defecto después de 8 horas; puedes cambiarlo con `SESSION_DURATION_HOURS` antes de iniciar Spring Boot.

## Primer acceso

Spring Boot crea la tabla `usuarios`, pero no crea usuarios predeterminados. En la pantalla de inicio de sesion, usa el enlace de registro para crear una cuenta operativa. El nombre de usuario debe ser unico y la contrasena debe tener al menos 8 caracteres.

Para crear el primer administrador, define su nombre de usuario antes de iniciar Spring Boot y registra esa cuenta desde la pantalla de registro:

```powershell
$env:BOOTSTRAP_ADMIN_USERNAME = "admin"
```

Solo una cuenta cuyo nombre coincida con esa variable recibe el rol `ADMINISTRADOR`. Después de iniciar sesion, el administrador puede abrir **Usuarios** para activar cuentas y cambiar roles. Las cuentas normales tienen el rol `OPERATIVO`.

## Funcionalidades conectadas

- **Inventario:** consulta de stock, indicadores, búsqueda por nombre o código y filtro por categoría.
- **Entradas:** crea productos nuevos o, si ya existe un producto activo con el mismo nombre, categoría y proveedor (sin distinguir mayúsculas/minúsculas), suma la cantidad a su stock existente en lugar de duplicarlo. El proveedor es obligatorio.
- **Códigos SKU:** se generan automáticamente al registrar una entrada, a partir de las iniciales del nombre del producto, las 3 primeras letras de la categoría y las 3 primeras letras del proveedor (sin acentos ni espacios), seguidas de un consecutivo de 4 dígitos (por ejemplo `THFERACM-0001`). El código nunca se solicita ni se muestra en el formulario antes de guardar.
- **Salidas:** descuenta existencias y rechaza cantidades superiores al stock disponible.
- **Historial:** filtra movimientos por fechas, tipo y producto, mostrando usuario y fecha.
- **Usuarios:** gestión administrativa de roles y estado de las cuentas.
- **Reportes:** el resumen del inventario muestra productos, unidades, stock bajo, agotados y cantidad de movimientos; además puedes descargar CSV del inventario y de los movimientos filtrados.

Si Angular se sirve desde un origen distinto de `http://localhost:4200`, configura la variable `FRONTEND_ORIGINS` antes de iniciar el backend:

```powershell
$env:FRONTEND_ORIGINS = "http://localhost:4300"
```

## Comandos utiles

Ejecuta estos comandos desde la raiz del repositorio:

```bash
# Compilar el frontend
npm run build

# Ejecutar las pruebas del frontend sin modo interactivo
npm test -- --watch=false
```

Para ejecutar las pruebas del backend:

En macOS o Linux:

```bash
./backend/mvnw -f backend/pom.xml test
```

En Windows:

```powershell
.\backend\mvnw.cmd -f backend\pom.xml test
```

Las pruebas del backend usan una base H2 en memoria aislada, por lo que no necesitan MySQL ni Docker.

Para generar un componente Angular:

```bash
npx ng generate component nombre-del-componente
```

## Limpieza de cuentas antiguas

Si una instalacion anterior creo cuentas de prueba, puedes eliminarlas una sola vez con:

```sql
DELETE FROM stock_flow.usuarios
WHERE usuario IN ('admin', 'estudiante', 'walter');
```

## Solucion de problemas

- **No se puede conectar con MySQL:** verifica que el servicio este iniciado, que el puerto sea `3306` y que `DB_USERNAME` y `DB_PASSWORD` sean correctos.
- **El frontend no llega al backend:** confirma que Spring Boot este ejecutandose en el puerto `8080` y que Angular este en el origen permitido por `FRONTEND_ORIGINS`.
- **Falla Maven por la version de Java:** comprueba que `java -version` muestre JDK 25 y que `JAVA_HOME` apunte a esa instalacion.
- **Faltan paquetes de Angular:** ejecuta `npm install` desde la raiz y vuelve a ejecutar `npm start`.

## Recursos

- [Documentacion tecnica: arquitectura, API, observaciones y despliegue](DOCUMENTACION_TECNICA.md)
- Para desplegar en Railway, crea dos servicios desde la raiz del repositorio: selecciona `Dockerfile.backend` para el backend y `Dockerfile.frontend` para Angular SSR. Configura `API_BASE_URL` y `NG_ALLOWED_HOSTS` en el frontend; `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` y `FRONTEND_ORIGINS` en el backend. Consulta la documentacion tecnica para el orden de configuracion y validacion.
- [Documentacion de Angular CLI](https://angular.dev/tools/cli)
- [Documentacion de Spring Boot](https://docs.spring.io/spring-boot/index.html)
- [Documentacion de MySQL](https://dev.mysql.com/doc/)
