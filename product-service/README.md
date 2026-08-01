# product-service

Microservicio de **catálogo de productos** de la tienda de rock. Expone una API REST CRUD y un endpoint para consultar los productos asociados a un cliente. Se registra en Eureka con el nombre lógico `PRODUCT-SERVICE`.

- **Puerto:** `8082`
- **Base de datos:** H2 in-memory (`jdbc:h2:mem:productdb`)
- **Rol en Feign:** es *proveedor* — lo consume `customer-service`.

---

## 1. Modelo de dominio

`Product`

| Campo         | Tipo         | Notas                                            |
|---------------|--------------|--------------------------------------------------|
| `id`          | Long         | PK, `GenerationType.IDENTITY`                    |
| `nombre`      | String       | not null                                         |
| `descripcion` | String       |                                                  |
| `categoria`   | `Categoria`  | enum: `INSTRUMENTO`, `MERCH`, `INDUMENTARIA`     |
| `banda`       | String       | nullable (p.ej. una campera genérica no tiene banda) |
| `precio`      | BigDecimal   | precision=12, scale=2, ≥ 0                       |
| `stock`       | Integer      | ≥ 0                                              |
| `customerId`  | Long         | nullable (producto en stock sin dueño asignado)  |
| `fechaCompra` | LocalDate    | nullable                                         |

Tabla física: `products`.

---

## 2. Dependencias clave (`pom.xml`)

| Dependencia                                        | Rol                          |
|----------------------------------------------------|------------------------------|
| `spring-boot-starter-web`                          | REST                         |
| `spring-boot-starter-data-jpa`                     | JPA + Hibernate              |
| `spring-boot-starter-validation`                   | Jakarta Bean Validation      |
| `spring-cloud-starter-config`                      | Cliente Config Server        |
| `spring-cloud-starter-netflix-eureka-client`       | Registro en Eureka           |
| `com.h2database:h2` (runtime)                      | Base H2 in-memory            |

---

## 3. Configuración

### Local (`src/main/resources/application.yml`)

```yaml
spring:
  application:
    name: product-service
  config:
    import: "configserver:http://localhost:8888"
```

### Remota (`config/product-service.yml`)

```yaml
server:
  port: 8082

spring:
  datasource:
    url: jdbc:h2:mem:productdb
    driver-class-name: org.h2.Driver
    username: sa
    password: ""
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    defer-datasource-initialization: true   # asegura que data.sql corra DESPUES del DDL
  h2:
    console:
      enabled: true
      path: /h2-console
```

---

## 4. Datos de seed (`src/main/resources/data.sql`)

Se cargan 6 productos: guitarra Les Paul, remera Iron Maiden, vinilo *Master of Puppets*, campera de cuero, bajo Fender y botas militares. Los `customer_id` (1, 2, 3) alinean con los clientes seedeados en `customer-service`.

---

## 5. Endpoints REST (`/products`)

| Método | Ruta                                | Body           | Response          |
|--------|-------------------------------------|----------------|-------------------|
| GET    | `/products`                         | —              | 200 lista         |
| GET    | `/products/{id}`                    | —              | 200 / 404         |
| GET    | `/products/customer/{customerId}`   | —              | 200 lista         |
| POST   | `/products`                         | `ProductRequestDTO` (validado) | 201 + `Location` |
| PUT    | `/products/{id}`                    | `ProductRequestDTO` (validado) | 200 / 404 |
| DELETE | `/products/{id}`                    | —              | 204 / 404         |

### DTOs

- **`ProductRequestDTO`** (record) — validaciones: `@NotBlank nombre`, `@NotNull categoria`, `@NotNull @DecimalMin("0.0") precio`, `@NotNull @Min(0) stock`.
- **`ProductResponseDTO`** (record) — mismos campos + `id`.

### Ejemplos `curl`

```bash
curl -s http://localhost:8082/products
curl -s http://localhost:8082/products/1
curl -s http://localhost:8082/products/customer/1

curl -s -H "Content-Type: application/json" -d '{
  "nombre":"Baqueta Vic Firth 5A",
  "descripcion":"Par de baquetas de hickory",
  "categoria":"INSTRUMENTO",
  "banda":null,
  "precio":8500,
  "stock":50,
  "customerId":null,
  "fechaCompra":null
}' http://localhost:8082/products

curl -s -X PUT -H "Content-Type: application/json" -d '{...}' http://localhost:8082/products/1
curl -s -X DELETE http://localhost:8082/products/5
```

---

## 6. Manejo de errores

`GlobalExceptionHandler` (`@RestControllerAdvice`):

| Excepción                          | Status | Ejemplo body                       |
|------------------------------------|--------|------------------------------------|
| `ProductNotFoundException`         | 404    | `"message":"Producto no encontrado con id: 999"` |
| `MethodArgumentNotValidException`  | 400    | `"details":["nombre: El nombre no puede estar vacio"]` |
| `Exception` (generic)              | 500    | `"message":"Error inesperado: ..."`|

Todos comparten el record `ErrorResponse(timestamp, status, error, message, path, details)`.

---

## 7. Estructura de paquetes

```
com.tp.productservice
├── ProductServiceApplication         # @SpringBootApplication
├── controller.ProductController      # @RestController
├── service.ProductService            # @Service (@Transactional)
├── repository.ProductRepository      # JpaRepository<Product, Long> + findByCustomerId
├── mapper.ProductMapper              # @Component  Entity <-> DTO
├── model.Product                     # @Entity
├── model.Categoria                   # enum
├── dto.ProductRequestDTO             # record + @Valid
├── dto.ProductResponseDTO            # record
└── exception.{ProductNotFoundException, ErrorResponse, GlobalExceptionHandler}
```

---

## 8. Cómo ejecutarlo

**Requisitos:** `config-server` y `eureka-server` ya arriba.

```bash
mvn -DskipTests package
java -jar target/product-service-1.0.0.jar
```

Al segundo intento hay que borrar el registro previo en Eureka o esperar 90s (evict).

---

## 9. Extras

- **H2 Console:** `http://localhost:8082/h2-console` con JDBC URL `jdbc:h2:mem:productdb`, user `SA`, sin password.
- Los datos se **pierden en cada arranque** (H2 in-memory).

