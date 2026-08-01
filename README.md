# TP Final — Ecosistema de Microservicios

Tienda de articulos de bandas de rock, instrumentos musicales e indumentaria metalera.

Este repositorio contiene:

- `config-server/` — Spring Cloud Config Server (puerto **8888**). Lee su configuracion desde la carpeta `config/` de este mismo repo.
- `eureka-server/` — Servidor de descubrimiento (puerto **8761**).
- `product-service/` — Gestiona productos, expone REST (puerto **8082**).
- `customer-service/` — Gestiona clientes; llama a `product-service` via Feign (puerto **8081**).
- `config/` — Archivos `.yml` consumidos por el Config Server.

> Scaffolding en progreso.
