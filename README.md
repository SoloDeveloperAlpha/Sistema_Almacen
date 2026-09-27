# SistemaAlmace

This project was generated using [Angular CLI](https://github.com/angular/angular-cli) version 22.1.5.

## Run the application

Install and start MySQL Server locally, then start the Spring Boot API. Spring creates the `stock_flow` database (if the configured MySQL account has permission) and the `usuarios` table.

In PowerShell, provide the MySQL credentials in the backend terminal before starting Spring Boot:

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "123456789"
.\backend\mvnw.cmd -f backend\pom.xml spring-boot:run
```

The defaults connect to `localhost:3306` and database `stock_flow`; override them with `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` when needed. For better security, use a dedicated MySQL account limited to `stock_flow` instead of `root`.

Start Angular in a second terminal:

```bash
npm start
```

Open `http://localhost:4200/`. Users and BCrypt password hashes are stored in MySQL. Spring creates the `usuarios` table, but does not create any users by default. Create the first account manually from the registration link on the login page. New accounts require a unique username and a password of at least 8 characters. Set `FRONTEND_ORIGINS` if Angular is served from a different origin.

If this application was previously started against MySQL with default accounts enabled, remove those old rows once:

```sql
DELETE FROM stock_flow.usuarios
WHERE usuario IN ('admin', 'estudiante', 'walter');
```

## Code scaffolding

Angular CLI includes powerful code scaffolding tools. To generate a new component, run:

```bash
ng generate component component-name
```

For a complete list of available schematics (such as `components`, `directives`, or `pipes`), run:

```bash
ng generate --help
```

## Building

To build the project run:

```bash
ng build
```

This will compile your project and store the build artifacts in the `dist/` directory. By default, the production build optimizes your application for performance and speed.

## Running unit tests

Run Angular tests with the [Vitest](https://vitest.dev/) test runner:

```bash
npm test -- --watch=false
```

Run Spring Boot tests from the repository root:

```bash
./backend/mvnw -f backend/pom.xml test
```

On Windows, use `backend\mvnw.cmd -f backend\pom.xml test`. Backend tests use an isolated in-memory H2 database and do not require Docker or MySQL.

## Running end-to-end tests

For end-to-end (e2e) testing, run:

```bash
ng e2e
```

Angular CLI does not come with an end-to-end testing framework by default. You can choose one that suits your needs.

## Additional Resources

For more information on using the Angular CLI, including detailed command references, visit the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.
