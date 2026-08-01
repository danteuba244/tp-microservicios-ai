# config-server

Spring Cloud **Config Server** con backend **Git (GitHub)**. Centraliza la configuración de todos los demás microservicios del ecosistema.

- **Puerto:** `8888`
- **Ubicación de config:** carpeta `config/` de este mismo repo, branch `main`.
- **Consumidores:** `eureka-server`, `product-service`, `customer-service`.

---

## 1. Rol dentro del ecosistema

Es el **primer servicio que debe arrancar**. Todos los demás usan `spring.config.import=configserver:http://localhost:8888` para leer su configuración remota antes de bootear.

```
GitHub (config/*.yml)
        │
        ▼
 config-server :8888  ──►  eureka-server / product-service / customer-service
```

---

## 2. Dependencias clave (`pom.xml`)

| Dependencia                          | Rol                                                  |
|--------------------------------------|------------------------------------------------------|
| `spring-cloud-config-server`         | Habilita el servidor de configuración                |
| `spring-boot-starter-parent 3.3.5`   | Padre Boot                                           |
| `spring-cloud-dependencies 2023.0.3` | BOM de Spring Cloud (Leyton)                         |

---

## 3. Configuración (`src/main/resources/application.yml`)

```yaml
server:
  port: 8888

spring:
  application:
    name: config-server
  cloud:
    config:
      server:
        git:
          uri: https://github.com/danteuba244/tp-microservicios-ai.git
          default-label: main
          search-paths: config
          clone-on-start: true
```

- `search-paths: config` → sirve solo lo que hay en la carpeta `config/`.
- `clone-on-start: true` → clona el repo al arrancar para fallar rápido si hay problema de red o credenciales.

---

## 4. Cómo ejecutarlo

```bash
mvn -DskipTests package
java -jar target/config-server-1.0.0.jar
```

---

## 5. Endpoints / cómo verificar que funciona

Los endpoints siguen la convención `/{application}/{profile}[/{label}]`.

```bash
# Config común a todos
curl -s http://localhost:8888/application/default

# Config específica de cada microservicio
curl -s http://localhost:8888/eureka-server/default
curl -s http://localhost:8888/product-service/default
curl -s http://localhost:8888/customer-service/default
```

La respuesta incluye un `propertySources` con la URL de GitHub y el commit hash del que se sirvió la config, por ejemplo:

```json
{
  "name": "product-service",
  "profiles": ["default"],
  "label": null,
  "version": "5a5ddd350e3937ee4f3abae9c17857bdf61e16dc",
  "propertySources": [
    {
      "name": "https://github.com/danteuba244/tp-microservicios-ai.git/config/product-service.yml",
      "source": { ... }
    }
  ]
}
```

---

## 6. Cómo actualizar la configuración

1. Editar cualquier archivo de `config/*.yml`.
2. `git commit` y `git push`.
3. **Reiniciar** el microservicio consumidor. El Config Server relee el repo por request, pero los clientes solo aplican cambios al bootear (este TP no incluye `@RefreshScope`).

---

## 7. Anotaciones destacadas del código

- `@SpringBootApplication` — arranque estándar.
- `@EnableConfigServer` — activa todos los endpoints REST del Config Server.

