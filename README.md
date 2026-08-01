# TP Final — Ecosistema de Microservicios (Spring Cloud + Eureka + Feign)

**Tema del negocio:** tienda online de merchandising de bandas de rock, instrumentos musicales e indumentaria metalera.

Este trabajo integra en un solo ecosistema: **Config Server (Git-backed)**, **Service Discovery (Eureka)**, dos microservicios de negocio (`product-service`, `customer-service`) con **REST + JPA + H2**, y **comunicación síncrona vía OpenFeign** entre ellos.

---

## 1. Arquitectura general

```
                    +--------------------------+
                    |  GitHub (config repo)    |
                    |  danteuba244/tp-micro..   |
                    |  carpeta config/         |
                    +------------+-------------+
                                 |
                                 | git pull on demand
                                 v
                    +--------------------------+
                    |  Config Server           |
                    |  :8888                   |
                    +------------+-------------+
                                 ^
                                 | HTTP (bootstrap config.import)
                                 |
       +-------------------------+-----------------------+
       |                         |                        |
       v                         v                        v
+--------------+          +--------------+         +--------------+
| Eureka       |<---------| product-svc  |         | customer-svc |
| Server :8761 | register | :8082 REST   |         | :8081 REST   |
|              |          | H2 productdb |         | H2 customerdb|
|              |          |              |         |              |
|              |<---------------- register ------->|              |
+--------------+          +--------------+         +--------------+
                                 ^                        |
                                 |                        |
                                 |  Feign (LoadBalancer)  |
                                 +------------------------+
                                     GET /products/customer/{id}
```

**Flujo end-to-end** (ejemplo `GET /customers/1/products`):
1. `customer-service` recibe el request.
2. Busca al cliente 1 en su H2 local.
3. Su `ProductClient` (Feign) resuelve el nombre lógico `product-service` contra Eureka.
4. Spring Cloud LoadBalancer elige una instancia y llama `GET /products/customer/1`.
5. `product-service` responde con los productos del cliente 1.
6. `customer-service` devuelve un `CustomerWithProductsDTO` combinando ambos.

---

## 2. Estructura del repositorio

```
tp-microservicios-ai/
├── config/                        # Configuración centralizada (Git-backed)
│   ├── application.yml            #   Config común (Eureka defaultZone)
│   ├── eureka-server.yml
│   ├── product-service.yml
│   └── customer-service.yml
├── config-server/                 # :8888  Spring Cloud Config Server
├── eureka-server/                 # :8761  Netflix Eureka
├── product-service/               # :8082  REST + JPA + H2 + Eureka client
├── customer-service/              # :8081  REST + JPA + H2 + Eureka + Feign
└── README.md
```

---

## 3. Stack técnico

| Componente             | Versión / Nota                                        |
|------------------------|-------------------------------------------------------|
| Java                   | 17 (Temurin)                                          |
| Spring Boot            | 3.3.5                                                 |
| Spring Cloud           | 2023.0.3 (Leyton)                                     |
| Base de datos          | H2 in-memory (una por microservicio de negocio)       |
| Service Discovery      | Netflix Eureka Server + Client                        |
| Config centralizada    | Spring Cloud Config Server (backend: GitHub)          |
| Comunicación entre µsvs| OpenFeign + Spring Cloud LoadBalancer                 |
| Validación             | `spring-boot-starter-validation` (Jakarta Validation) |
| Build                  | Maven 3.9.x                                           |

---

## 4. Puertos y URLs

| Servicio         | Puerto | Endpoint clave                                          |
|------------------|--------|---------------------------------------------------------|
| Config Server    | 8888   | `http://localhost:8888/{service}/default`               |
| Eureka Server    | 8761   | Dashboard: `http://localhost:8761`                      |
| product-service  | 8082   | `http://localhost:8082/products`                        |
| customer-service | 8081   | `http://localhost:8081/customers`                       |
| H2 console (product)  | 8082 | `http://localhost:8082/h2-console` (JDBC `jdbc:h2:mem:productdb`, user `SA`, sin pass) |
| H2 console (customer) | 8081 | `http://localhost:8081/h2-console` (JDBC `jdbc:h2:mem:customerdb`, user `SA`, sin pass) |

---

## 5. Cómo levantar el ecosistema

**Requisitos:** Java 17, Maven 3.9+, acceso a internet (Config Server clona el repo de GitHub en el arranque).

Compilar los cuatro proyectos (con los tests unitarios de `product-service` y `customer-service`):

```bash
cd config-server    ; mvn -DskipTests package ; cd ..
cd eureka-server    ; mvn -DskipTests package ; cd ..
cd product-service  ; mvn package ; cd ..
cd customer-service ; mvn package ; cd ..
```

> Para saltar los tests durante iteración rápida, agregar `-DskipTests` a los dos últimos comandos.

Arrancar **en este orden** (cada uno en su terminal):

```bash
# 1) Config Server
java -jar config-server/target/config-server-1.0.0.jar

# 2) Eureka Server (una vez que Config Server escuche en :8888)
java -jar eureka-server/target/eureka-server-1.0.0.jar

# 3) product-service
java -jar product-service/target/product-service-1.0.0.jar

# 4) customer-service
java -jar customer-service/target/customer-service-1.0.0.jar
```

El orden es importante: **Config Server** debe estar arriba antes que cualquier otro servicio, y **Eureka** antes de los microservicios de negocio.

---

## 6. Endpoints REST

### `product-service` (`:8082/products`)

| Método | Ruta                          | Descripción                        |
|--------|-------------------------------|------------------------------------|
| GET    | `/products`                   | Lista todos los productos          |
| GET    | `/products/{id}`              | Obtiene un producto por id         |
| GET    | `/products/customer/{customerId}` | Productos de un cliente        |
| POST   | `/products`                   | Crea un producto (`@Valid`)        |
| PUT    | `/products/{id}`              | Actualiza un producto (`@Valid`)   |
| DELETE | `/products/{id}`              | Elimina un producto                |

**Entidad `Product`:** `id, nombre, descripcion, categoria (INSTRUMENTO|MERCH|INDUMENTARIA), banda, precio, stock, customerId, fechaCompra`.

### `customer-service` (`:8081/customers`)

| Método | Ruta                        | Descripción                                             |
|--------|-----------------------------|---------------------------------------------------------|
| GET    | `/customers`                | Lista todos los clientes                                |
| GET    | `/customers/{id}`           | Obtiene un cliente por id                               |
| GET    | `/customers/{id}/products`  | **Agregación Feign**: cliente + sus productos           |
| POST   | `/customers`                | Crea un cliente (`@Valid`, email válido)                |
| PUT    | `/customers/{id}`           | Actualiza un cliente (`@Valid`)                         |
| DELETE | `/customers/{id}`           | Elimina un cliente                                      |

**Entidad `Customer`:** `id, nombre, apellido, email (unique), dni (unique), telefono, direccion, fechaRegistro`.

---

## 7. Ejemplos con `curl`

Todos los ejemplos asumen el ecosistema arriba.

### Listar productos
```bash
curl -s http://localhost:8082/products
```

### Productos de un cliente
```bash
curl -s http://localhost:8082/products/customer/1
```

### Producto inexistente → 404 con `ErrorResponse`
```bash
curl -s -w "\n%{http_code}\n" http://localhost:8082/products/999
```

### POST inválido → 400 con detalles de validación
```bash
curl -s -H "Content-Type: application/json" \
     -d '{"categoria":"MERCH","precio":100,"stock":1}' \
     http://localhost:8082/products
```

### **Agregación vía Feign** (cliente + sus productos)
```bash
curl -s http://localhost:8081/customers/1/products
```

Ejemplo de respuesta:
```json
{
  "customer": { "id": 1, "nombre": "Ozzy", "apellido": "Osbourne", ... },
  "products": [
    { "id": 1, "nombre": "Guitarra Gibson Les Paul Standard", "customerId": 1, ... },
    { "id": 2, "nombre": "Remera Iron Maiden - The Trooper",  "customerId": 1, ... }
  ]
}
```

### POST cliente
```bash
curl -s -H "Content-Type: application/json" \
     -d '{
       "nombre":"Bruce",
       "apellido":"Dickinson",
       "email":"bruce@ironmaiden.com",
       "dni":"40444444",
       "telefono":"+5411-4444-4444",
       "direccion":"Aeropuerto Ezeiza",
       "fechaRegistro":"2026-08-01"
     }' \
     http://localhost:8081/customers
```

---

## 8. Manejo centralizado de errores

Ambos microservicios exponen un `@RestControllerAdvice` con un cuerpo estándar:

```json
{
  "timestamp": "2026-07-31T22:39:04.453",
  "status": 404,
  "error": "Not Found",
  "message": "Cliente no encontrado con id: 999",
  "path": "/customers/999",
  "details": null
}
```

`customer-service` además maneja específicamente `FeignException`:  
si `product-service` está caído o responde con error, `GET /customers/{id}/products` devuelve **HTTP 502 Bad Gateway** con la causa Feign en `details`.

---

## 9. Datos de seed (H2 in-memory)

**Clientes (`customer-service`)**
| id | Nombre           | Email                       |
|----|------------------|-----------------------------|
| 1  | Ozzy Osbourne    | ozzy@blacksabbath.com       |
| 2  | Ronnie Dio       | rjd@rainbow.com             |
| 3  | Lemmy Kilmister  | lemmy@motorhead.com         |

**Productos (`product-service`)**
| id | Nombre                              | Categoría     | customerId |
|----|-------------------------------------|---------------|------------|
| 1  | Guitarra Gibson Les Paul Standard   | INSTRUMENTO   | 1          |
| 2  | Remera Iron Maiden - The Trooper    | MERCH         | 1          |
| 3  | Vinilo Master of Puppets            | MERCH         | 2          |
| 4  | Campera de cuero clásica            | INDUMENTARIA  | 2          |
| 5  | Bajo Fender Precision Bass          | INSTRUMENTO   | null       |
| 6  | Botas militares negras              | INDUMENTARIA  | 3          |

Los datos se recargan en cada arranque (H2 in-memory). Para persistir cambios entre arranques habría que migrar a H2 en archivo o PostgreSQL — se dejó fuera por el alcance del TP.

---

## 10. Configuración centralizada — cómo verificar

El Config Server sirve archivos desde `config/` en el branch `main` del repo `danteuba244/tp-microservicios-ai`.

```bash
# Ver lo que ve el config server para cada servicio
curl -s http://localhost:8888/eureka-server/default    | jq .
curl -s http://localhost:8888/product-service/default  | jq .
curl -s http://localhost:8888/customer-service/default | jq .
```

Cualquier cambio pusheado a `config/*.yml` se refleja al reiniciar el microservicio consumidor (o dinámicamente si se agrega `spring-boot-starter-actuator` + `@RefreshScope`, fuera del alcance de este TP).

---

## 11. Cumplimiento del enunciado

| Requisito del TP                                                                 | Estado |
|----------------------------------------------------------------------------------|--------|
| Config Server con Git como backend, sirviendo config a los demás                 | ✅     |
| Eureka Server con dashboard funcional                                            | ✅     |
| ≥ 2 microservicios de negocio                                                    | ✅ (product + customer) |
| Cada uno con al menos 5 endpoints REST                                           | ✅ (6 c/u incl. GET colección, GET por id, POST, PUT, DELETE, GET filtrado) |
| Comunicación entre microservicios vía OpenFeign                                  | ✅ (`ProductClient` en customer-service) |
| Registro en Eureka de los microservicios de negocio                              | ✅ (`PRODUCT-SERVICE`, `CUSTOMER-SERVICE` UP) |
| DTOs con validaciones                                                            | ✅ (`@NotBlank`, `@Email`, `@DecimalMin`, `@Min`, `@NotNull`) |
| Mapeo Entidad ⇔ DTO en capa dedicada                                             | ✅ (`ProductMapper`, `CustomerMapper`) |
| Manejo de excepciones global (not found + validación + Feign)                    | ✅ (`GlobalExceptionHandler` en ambos) |
| Persistencia en base de datos                                                    | H2 in-memory (opcional del enunciado, no PostgreSQL) |

---

## 12. Repositorio

`https://github.com/danteuba244/tp-microservicios-ai`

Autor: **Dante Casalla**
