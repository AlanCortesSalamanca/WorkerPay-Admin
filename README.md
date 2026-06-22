# WorkerPay Admin

Sistema web interno para administrar trabajadores, pagos, adelantos, deudas, abonos, descuentos y reportes administrativos.

## Stack tecnologico

- Java 17
- Spring Boot
- Spring MVC
- Spring Data JPA
- Spring Security
- Thymeleaf
- Maven
- PostgreSQL local
- HTML, CSS y JavaScript separados

## Modulos

- Autenticacion con usuarios y roles en base de datos.
- Dashboard protegido.
- CRUD de trabajadores.
- Estructura base para adelantos, deudas, pagos y reportes.
- Reportes placeholder para futuras fases.

## Requisitos previos

- Java 17 instalado.
- PostgreSQL instalado localmente.
- Base de datos `workerpay_db` creada.

## Crear base de datos local

En PostgreSQL:

```sql
CREATE DATABASE workerpay_db;
```

Opcionalmente, puedes crear un usuario especifico para el proyecto:

```sql
CREATE USER workerpay_user WITH PASSWORD 'TU_PASSWORD_LOCAL';
GRANT ALL PRIVILEGES ON DATABASE workerpay_db TO workerpay_user;
```

Luego conectate a la base:

```sql
\c workerpay_db
```

Y otorga permisos al schema `public`:

```sql
GRANT ALL ON SCHEMA public TO workerpay_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT ALL ON TABLES TO workerpay_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT ALL ON SEQUENCES TO workerpay_user;
```

`TU_PASSWORD_LOCAL` es solo un ejemplo para desarrollo local. No lo subas al repositorio.

## Variables de entorno

La aplicacion lee:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `APP_ADMIN_USERNAME`
- `APP_ADMIN_PASSWORD`

El usuario administrador inicial se crea automaticamente si todavia no existe ningun usuario con rol `ADMIN`. La contrasena se guarda con BCrypt.

### Windows PowerShell

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/workerpay_db"
$env:DB_USERNAME="workerpay_user"
$env:DB_PASSWORD="TU_PASSWORD_LOCAL"
$env:APP_ADMIN_USERNAME="admin"
$env:APP_ADMIN_PASSWORD="TU_PASSWORD_ADMIN_LOCAL"

.\mvnw.cmd spring-boot:run
```

### Windows CMD

```bat
set DB_URL=jdbc:postgresql://localhost:5432/workerpay_db
set DB_USERNAME=workerpay_user
set DB_PASSWORD=TU_PASSWORD_LOCAL
set APP_ADMIN_USERNAME=admin
set APP_ADMIN_PASSWORD=TU_PASSWORD_ADMIN_LOCAL

.\mvnw.cmd spring-boot:run
```

### Linux / macOS

```bash
export DB_URL="jdbc:postgresql://localhost:5432/workerpay_db"
export DB_USERNAME="workerpay_user"
export DB_PASSWORD="TU_PASSWORD_LOCAL"
export APP_ADMIN_USERNAME="admin"
export APP_ADMIN_PASSWORD="TU_PASSWORD_ADMIN_LOCAL"

./mvnw spring-boot:run
```

Estas variables duran solo en la terminal donde las configuras.

## Ejecutar

### Windows PowerShell

Compilar:

```powershell
.\mvnw.cmd clean install
```

Iniciar la aplicacion:

```powershell
.\mvnw.cmd spring-boot:run
```

### Linux / macOS

Compilar:

```bash
./mvnw clean install
```

Iniciar la aplicacion:

```bash
./mvnw spring-boot:run
```

Si tienes Maven instalado globalmente, tambien puedes usar:

```bash
mvn clean install
mvn spring-boot:run
```

Abrir:

```text
http://localhost:8080/login
```

Entra con el usuario y contrasena configurados mediante:

- `APP_ADMIN_USERNAME`
- `APP_ADMIN_PASSWORD`

## Rutas principales

- `/login`
- `/dashboard`
- `/workers`
- `/workers/new`
- `/workers/{id}`
- `/workers/{id}/edit`
- `/reports`

## Proximas fases

- Adelantos.
- Deudas.
- Pagos.
- Reportes completos.
- Exportacion PDF/Excel.
- Auditoria de movimientos.
