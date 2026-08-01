# eureka-server

**Netflix Eureka Server** — servicio de descubrimiento (Service Discovery) del ecosistema. Los microservicios de negocio se registran acá y usan este registro para resolverse entre sí (Feign + LoadBalancer).

- **Puerto:** `8761`
- **Dashboard:** `http://localhost:8761`

---

## 1. Rol dentro del ecosistema

Segundo servicio en arrancar (después del Config Server). Recibe:
- Registros de `PRODUCT-SERVICE` y `CUSTOMER-SERVICE`.
- Peticiones de descubrimiento del Feign client de `customer-service`.

```
     product-service ─┐                          ┌─► product-service
                      ├─ register / heartbeat ──►│
     customer-service ┘         ▲                └─► customer-service
                                │                        │
                                └── discovery (Feign) ◄──┘
```

---

## 2. Dependencias clave (`pom.xml`)

| Dependencia                                        | Rol                                         |
|----------------------------------------------------|---------------------------------------------|
| `spring-cloud-starter-netflix-eureka-server`       | Servidor Eureka                             |
| `spring-cloud-starter-config`                      | Cliente de Config Server (lee su propia yml)|
| `spring-boot-starter-parent 3.3.5`                 | Padre Boot                                  |
| `spring-cloud-dependencies 2023.0.3`               | BOM Spring Cloud                            |

---

## 3. Configuración

### Local (`src/main/resources/application.yml`)

Solo un mínimo bootstrap: nombre y de dónde leer el resto.

```yaml
spring:
  application:
    name: eureka-server
  config:
    import: "configserver:http://localhost:8888"
```

### Remota, servida por el Config Server (`config/eureka-server.yml`)

```yaml
server:
  port: 8761

eureka:
  client:
    register-with-eureka: false   # este es el servidor: no se registra a si mismo
    fetch-registry: false
```

Como `application.yml` (común) define `eureka.client.serviceUrl.defaultZone: http://localhost:8761/eureka/`, esta config desactiva ese comportamiento solo para el propio Eureka Server.

---

## 4. Cómo ejecutarlo

**El Config Server debe estar arriba primero** (`localhost:8888`).

```bash
mvn -DskipTests package
java -jar target/eureka-server-1.0.0.jar
```

---

## 5. Verificar que funciona

- Dashboard HTML: `http://localhost:8761`
- API XML: `curl http://localhost:8761/eureka/apps`
- API JSON: `curl -H "Accept: application/json" http://localhost:8761/eureka/apps`

Cuando todo esté arriba se ven las dos apps registradas:

```
CUSTOMER-SERVICE   UP   192.168.x.x:customer-service:8081
PRODUCT-SERVICE    UP   192.168.x.x:product-service:8082
```

Cada instancia manda un heartbeat cada **30s**; si Eureka no recibe heartbeats la marca `DOWN`.

---

## 6. Anotaciones destacadas del código

- `@SpringBootApplication` — arranque estándar.
- `@EnableEurekaServer` — habilita el server, sus endpoints REST y el dashboard.

