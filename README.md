# Microservicio de Notificaciones - Pedidos360 (notifications-service)

Microservicio desarrollado en **Java 17** con **Spring Boot** para el procesamiento asíncrono y despacho de notificaciones por correo electrónico en el sistema **Pedidos360**, integrado mediante colas y Dead Letter Queues (DLQ) en **RabbitMQ**.

---

## 🚀 Requisitos Previos

- **Java**: JDK 17 o 21
- **Maven**: 3.8+ o Maven Wrapper (`mvnw.cmd`)
- **RabbitMQ**: Broker en ejecución (puerto 5672)
- **Puerto del servicio**: `8082`

---

## ⚙️ Configuración del Entorno (`application.properties`)

```properties
server.port=8082
spring.application.name=notificaciones-api

# Conexión a RabbitMQ Broker
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=pedidos360_user
spring.rabbitmq.password=pedidos360_pass

# Topología Centralizada de Mensajería
pedidos360.rabbitmq.exchange.pedidos=pedidos.exchange
pedidos360.rabbitmq.exchange.dlx=pedidos.dlx.exchange
pedidos360.rabbitmq.queue.notificaciones=pedidos.notificaciones.queue
pedidos360.rabbitmq.queue.notificaciones-dlq=pedidos.notificaciones.dlq
pedidos360.rabbitmq.routing-key.creado=pedidos.pedido.creado
pedidos360.rabbitmq.routing-key.notificaciones-dlq=pedidos.notificaciones.dlq
```

---

## 🛠️ Cómo Compilar y Ejecutar

1. **Compilar el proyecto**:
   ```bash
   .\mvnw.cmd clean package -DskipTests
   ```

2. **Ejecutar localmente**:
   ```bash
   .\mvnw.cmd spring-boot:run
   ```

---

## 📌 Endpoints REST Disponibles

### 1. Estado del Microservicio
- **Método:** `GET`
- **Ruta:** `/api/notificaciones/status`
- **Respuesta (`200 OK`):**
  ```json
  {
    "status": "OK",
    "message": "Microservicio de Notificaciones en ejecución",
    "port": "8082",
    "version": "1.0.0"
  }
  ```

### 2. Disparo Directo de Notificación (Prueba Postman)
- **Método:** `POST`
- **Ruta:** `/api/notificaciones/enviar`
- **Cabecera:** `Content-Type: application/json`
- **Cuerpo (`Body JSON`):**
  ```json
  {
    "destinatario": "alumno@duocuc.cl",
    "asunto": "Prueba de Evaluación - Notificación Pedidos360",
    "mensaje": "Confirmación formal de orden de compra generada exitosamente."
  }
  ```
- **Respuesta (`201 CREATED`):**
  ```json
  {
    "status": "SENT",
    "messageId": "NOTIF-POSTMAN-8F3A2B1C",
    "destinatario": "alumno@duocuc.cl",
    "asunto": "Prueba de Evaluación - Notificación Pedidos360",
    "origen": "POSTMAN_DIRECT_API",
    "fechaEnvio": "2026-10-06T12:35:00",
    "detalle": "Notificación manual enviada exitosamente vía API REST"
  }
  ```

### 3. Historial de Notificaciones Emitidas
- **Método:** `GET`
- **Ruta:** `/api/notificaciones/historial`

---

## 🐰 Mensajería Asíncrona (RabbitMQ)

- **Cola de entrada:** `pedidos.notificaciones.queue`
- **Dead Letter Queue:** `pedidos.notificaciones.dlq`
- **Confirmación:** Manual explícita (`basicAck`) y desvío a DLQ con `basicNack` sin requeue ante fallos.
