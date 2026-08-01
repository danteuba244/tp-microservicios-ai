# customer-service

Microservicio de **clientes** de la tienda de rock. Expone CRUD REST y un **endpoint de agregación** que combina la información del cliente con sus productos, consultando `product-service` mediante **OpenFeign**. Se registra en Eureka como `CUSTOMER-SERVICE`.

- **Puerto:** `8081`
- **Base de datos:** H2 in-memory (`jdbc:h2:mem:customerdb`)
- **Rol en Feign:** es *consumidor* — llama a `PRODUCT-SERVICE`.

---

## 1. Modelo de dominio

`Customer`

| Campo           | Tipo      | Notas                     |
|-----------------|-----------|---------------------------|
| `id`            | Long      | PK, `GenerationType.IDENTITY` |
| `nombre`        | String    | not null                  |
| `apellido`      | String    | not null                  |
| `email`         | String    | not null, **unique**      |
| `dni`           | String    | not null, **unique**      |
| `telefono`      | String    | nullable                  |
| `direccion`     | String    | nullable                  |
| `fechaRegistro` | LocalDate | not null                  |

Tabla física: `customers`.

---

## 2. Dependencias clave (`pom.xml`)

| Dependencia                                        | Rol                                    |
|----------------------------------------------------|----------------------------------------|
| `spring-boot-starter-web`                          | REST                                   |
| `spring-boot-starter-data-jpa`                     | JPA + Hibernate                        |
| `spring-boot-starter-validation`                   | Jakarta Bean Validation                |
| `spring-cloud-starter-config`                      | Cliente Config Server                  |
| `spring-cloud-starter-netflix-eureka-client`       | Registro / lookup en Eureka            |
| **`spring-cloud-starter-openfeign`**               | **Feign client** hacia `product-service` |
| `com.h2database:h2` (runtime)                      | Base H2 in-memory                      |

---

## 3. Configuración

### Local (`src/main/resources/application.yml`)

```yaml
spring:
  application:
    name: customer-service
  config:
    import: "configserver:http://localhost:8888"
```

### Remota (`config/customer-service.yml`)

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:h2:mem:customerdb
    driver-class-name: org.h2.Driver
    username: sa
    password: ""
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    defer-datasource-initialization: true
  h2:
    console:
      enabled: true
      path: /h2-console
```

---

## 4. Datos de seed (`src/main/resources/data.sql`)

Tres clientes emblemáticos del rock cuyos `id` (1, 2, 3) matchean con los `customer_id` seedeados en `product-service`:

| id | Nombre           |
|----|------------------|
| 1  | Ozzy Osbourne    |
| 2  | Ronnie Dio       |
| 3  | Lemmy Kilmister  |

---

## 5. Endpoints REST (`/customers`)

| Método | Ruta                        | Body                           | Response         |
|--------|-----------------------------|--------------------------------|------------------|
| GET    | `/customers`                | —                              | 200 lista        |
| GET    | `/customers/{id}`           | —                              | 200 / 404        |
| GET    | `/customers/{id}/products`  | —                              | 200 agregado (Feign) / 404 / 502 |
| POST   | `/customers`                | `CustomerRequestDTO` (validado)| 201 + `Location` |
| PUT    | `/customers/{id}`           | `CustomerRequestDTO` (validado)| 200 / 404        |
| DELETE | `/customers/{id}`           | —                              | 204 / 404        |

### DTOs

- **`CustomerRequestDTO`** (record) — validaciones: `@NotBlank nombre/apellido/dni`, `@NotBlank @Email email`, `@NotNull fechaRegistro`.
- **`CustomerResponseDTO`** (record) — todos los campos + `id`.
- **`ProductDTO`** (record) — copia local del `ProductResponseDTO` de `product-service`, usada para deserializar la respuesta Feign.
- **`CustomerWithProductsDTO`** (record) — `{ customer: CustomerResponseDTO, products: List<ProductDTO> }`.

### Ejemplos `curl`

```bash
curl -s http://localhost:8081/customers
curl -s http://localhost:8081/customers/1

# *** Agregación via Feign ***
curl -s http://localhost:8081/customers/1/products

curl -s -H "Content-Type: application/json" -d '{
  "nombre":"Bruce",
  "apellido":"Dickinson",
  "email":"bruce@ironmaiden.com",
  "dni":"40444444",
  "telefono":"+5411-4444-4444",
  "direccion":"Aeropuerto Ezeiza",
  "fechaRegistro":"2026-08-01"
}' http://localhost:8081/customers
```

---

## 6. Integración Feign — `ProductClient`

```java
@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/products/customer/{customerId}")
    List<ProductDTO> getProductsByCustomer(@PathVariable("customerId") Long customerId);
}
```

- El nombre `product-service` se resuelve **por Eureka** (no hay URL fija).
- Spring Cloud LoadBalancer elige una instancia.
- `@EnableFeignClients` en `CustomerServiceApplication` activa el escaneo.

Flujo del endpoint `/customers/{id}/products`:

```
CustomerController
  └─► CustomerService.findWithProducts(id)
        ├─► CustomerRepository.findById(id)              (JPA local)
        └─► ProductClient.getProductsByCustomer(id)      (Feign → Eureka → product-service)
```

---

## 7. Manejo de errores

`GlobalExceptionHandler` (`@RestControllerAdvice`):

| Excepción                          | Status | Cuándo                                                 |
|------------------------------------|--------|--------------------------------------------------------|
| `CustomerNotFoundException`        | 404    | Cliente inexistente                                    |
| `MethodArgumentNotValidException`  | 400    | Validación de DTO falla (`details` con la lista)       |
| **`FeignException`**               | **502**| **product-service caído o respondió error** → `Bad Gateway` con `feignStatus=...` en `details` |
| `Exception` (generic)              | 500    | Fallback                                               |

Todos usan el record `ErrorResponse(timestamp, status, error, message, path, details)`.

Para probar el fallback Feign: bajar `product-service` y hacer `curl http://localhost:8081/customers/1/products` → **502**.

---

## 8. Estructura de paquetes

```
com.tp.customerservice
├── CustomerServiceApplication         # @SpringBootApplication + @EnableFeignClients
├── controller.CustomerController      # @RestController
├── service.CustomerService            # @Service (@Transactional) — orquesta Feign
├── repository.CustomerRepository      # JpaRepository<Customer, Long>
├── mapper.CustomerMapper              # @Component  Entity <-> DTO
├── model.Customer                     # @Entity
├── client.ProductClient               # @FeignClient(name = "product-service")
├── dto.{CustomerRequestDTO, CustomerResponseDTO, ProductDTO, CustomerWithProductsDTO}
└── exception.{CustomerNotFoundException, ErrorResponse, GlobalExceptionHandler}
```

---

## 9. Cómo ejecutarlo

**Requisitos:** `config-server`, `eureka-server` y `product-service` ya arriba (product-service es necesario para probar la agregación Feign).

```bash
mvn -DskipTests package
java -jar target/customer-service-1.0.0.jar
```

---

## 10. Extras

- **H2 Console:** `http://localhost:8081/h2-console` con JDBC URL `jdbc:h2:mem:customerdb`, user `SA`, sin password.
- Los datos se **pierden en cada arranque** (H2 in-memory).

