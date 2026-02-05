# StockFlow Backend

Sistema de gestión de inventarios (API REST) construido con Java 21 y Spring Boot 3.2. Estructurado como proyecto multi‑módulo Maven, expone endpoints para autenticación, usuarios, productos y categorías, con seguridad basada en JWT y envoltura de respuestas uniforme.

## Arquitectura
- Módulos Maven:
  - `stockflow-backend-entity`: Entidades JPA, DTOs, utilidades y excepciones.
  - `stockflow-backend-web-api`: Controladores REST, servicios, repositorios, seguridad (JWT), configuración y arranque.
- Estándares:
  - Java 21, Spring Web, Spring Security, Spring Data JPA
  - Base de datos: MySQL (dialecto MySQL8)
  - Documentación: springdoc-openapi (Swagger UI)

## Requisitos
- Java 21 (JDK)
- Maven 3.9+
- MySQL con esquema y tablas ya existentes (JPA usa `validate`)
- Variables de entorno configuradas:
  - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
  - `JWT_SECRET_KEY`, `JWT_EXPIRATION` (ms)

Configuración referencia: [application.properties](stockflow-backend-web-api/src/main/resources/application.properties)

## Ejecución
- Construir todo:
  ```powershell
  mvn clean install -DskipTests
  ```
- Levantar solo el API (web-api):
  ```powershell
  mvn -pl stockflow-backend-web-api spring-boot:run
  ```
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Seguridad y Autenticación
- Filtro JWT valida el token en cada petición: [JwtAuthenticationFilter.java](stockflow-backend-web-api/src/main/java/com/stockflow/core/security/jwt/JwtAuthenticationFilter.java)
- Generación/validación de tokens: [JwtTokenProvider.java](stockflow-backend-web-api/src/main/java/com/stockflow/core/security/jwt/JwtTokenProvider.java)
- Rutas públicas y protegidas: [SecurityConfig.java](stockflow-backend-web-api/src/main/java/com/stockflow/core/config/SecurityConfig.java)
  - Público: `/api/auth/**`, Swagger (`/v3/api-docs/**`, `/swagger-ui/**`)
  - Protegido: todo lo demás (requiere `Authorization: Bearer <token>`)
- Password hashing: BCrypt en [ApplicationConfig.java](stockflow-backend-web-api/src/main/java/com/stockflow/core/config/ApplicationConfig.java)

### Flujo de Login
- Endpoint: `POST /api/auth/login`
- Body:
  ```json
  { "email": "usuario@correo.com", "password": "clave" }
  ```
- Respuesta: `200 OK`
  ```json
  { "token": "<JWT>" }
  ```
- Usar en llamadas posteriores:
  - Header: `Authorization: Bearer <JWT>`

## Respuestas API
- Todas las respuestas se envuelven en `ApiResponse`: [ApiResponse.java](stockflow-backend-web-api/src/main/java/com/stockflow/core/utils/common/ApiResponse.java)
  - Campos: `error`, `codigo`, `titulo`, `mensaje`, `fecha`, `data`

## Endpoints Principales
Los controladores siguen un controlador genérico con métodos estándar: [GenericController.java](stockflow-backend-web-api/src/main/java/com/stockflow/core/utils/common/GenericController.java)
- Crear: `POST /api/<recurso>`
- Actualizar: `PUT /api/<recurso>`
- Eliminar: `DELETE /api/<recurso>`
- Obtener por ID: `GET /api/<recurso>/{id}`
- Listar: `GET /api/<recurso>`

### Usuarios (`/api/usuarios`)
- `GET /{id}`: Obtener usuario por ID. [UsuarioController.java](stockflow-backend-web-api/src/main/java/com/stockflow/core/controller/UsuarioController.java)
- `GET /`: Listar usuarios.
- `POST /`: Crear usuario (contraseña se guarda con BCrypt).
- `PUT /`: Actualizar usuario (opcional cambiar contraseña).
- `DELETE /`: Eliminar usuario.
- `GET /findByNameUser/{email}`: Buscar por email.

### Categorías (`/api/categorias`)
- `GET /{id}`, `GET /`, `POST /`, `PUT /`, `DELETE /`. [CategoriaController.java](stockflow-backend-web-api/src/main/java/com/stockflow/core/controller/CategoriaController.java)

### Productos (`/api/productos`)
- `GET /{id}`, `GET /`, `POST /`, `PUT /`, `DELETE /`.
- `GET /findByCodigo/{codigo}`: Buscar por código. [ProductoController.java](stockflow-backend-web-api/src/main/java/com/stockflow/core/controller/ProductoController.java)

## Modelo de Datos (resumen)
- Usuario: `id`, `codigo`, `nombre`, `email`, `contrasena(WRITE_ONLY)`, `rol`, `estado`, `version`. [Usuario.java](stockflow-backend-entity/src/main/java/com/stockflow/core/entity/Usuario.java)
- Producto, Categoría y demás entidades disponibles en el módulo `entity`.
- DTOs controlan qué campos se serializan: [UsuarioDto.java](stockflow-backend-entity/src/main/java/com/stockflow/core/dto/UsuarioDto.java)

## Manejo de Errores
- Integridad referencial (MySQL 1451): `409 Conflict`. [GlobalExceptionHandler.java](stockflow-backend-web-api/src/main/java/com/stockflow/core/handler/GlobalExceptionHandler.java)
- Conflicto de versión (Optimistic Locking): `409 Conflict` con datos actuales en `data`.

## Buenas Prácticas y Convenciones
- JPA `ddl-auto=validate`: migraciones externas, no autogenerar tablas.
- Nombres físicos estándar: `PhysicalNamingStrategyStandardImpl`.
- Logging de seguridad y SQL habilitado para desarrollo.
- Servicios encapsulan validaciones y hashing: [UsuarioServiceImpl.java](stockflow-backend-web-api/src/main/java/com/stockflow/core/service/impl/UsuarioServiceImpl.java)

## Desarrollo Rápido
- Variables de entorno (PowerShell):
  ```powershell
  $env:DB_URL="jdbc:mysql://localhost:3306/stockflow?serverTimezone=UTC"
  $env:DB_USERNAME="root"
  $env:DB_PASSWORD="<tu_password>"
  $env:JWT_SECRET_KEY="<cadena-secreta-robusta>"
  $env:JWT_EXPIRATION="86400000" # 24h
  ```
- Ejecutar: `mvn -pl stockflow-backend-web-api spring-boot:run`

---
Estado actual: Autenticación JWT operativa, CRUD básico para Usuarios/Productos/Categorías, documentación Swagger y manejo centralizado de errores. Listo para integrar más módulos de inventario (movimientos, órdenes, ubicaciones, proveedores).