package com.pedidos360.notificaciones.service;

import com.pedidos360.notificaciones.dto.NotificacionRequestDTO;
import com.pedidos360.notificaciones.dto.NotificacionResponseDTO;
import com.pedidos360.notificaciones.dto.PedidoEventoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Servicio de envío y simulación de correos electrónicos transaccionales.
 * Mantiene un registro en memoria de todas las notificaciones emitidas (por evento o Postman).
 */
@Service
public class NotificacionEmailService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionEmailService.class);

    @Value("${pedidos360.notificaciones.remitente:noreply@pedidos360.cloud}")
    private String remitente;

    private final List<NotificacionResponseDTO> historial = new CopyOnWriteArrayList<>();

    /**
     * Procesa y despacha la notificación por correo ante un evento de Pedido Creado de RabbitMQ.
     */
    public NotificacionResponseDTO enviarConfirmacionPedido(PedidoEventoDTO pedido) {
        String messageId = "NOTIF-EVT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String asunto = "¡Tu pedido " + pedido.getPedidoId() + " ha sido recibido! - Pedidos360";

        StringBuilder cuerpo = new StringBuilder();
        cuerpo.append("Estimado(a) cliente, confirmamos la recepción de tu compra.\n");
        cuerpo.append("Número de Pedido: ").append(pedido.getPedidoId()).append("\n");
        cuerpo.append("Dirección de Envío: ").append(pedido.getDireccionEnvio()).append("\n");
        cuerpo.append("Total Facturado: $").append(pedido.getTotalPagar()).append("\n");
        cuerpo.append("Ítems Adquiridos: ").append(pedido.getItems() != null ? pedido.getItems().size() : 0).append("\n");

        logEmailBanner(messageId, pedido.getClienteEmail(), asunto, cuerpo.toString(), "RABBITMQ_EVENT");

        NotificacionResponseDTO response = new NotificacionResponseDTO(
                "SENT",
                messageId,
                pedido.getClienteEmail(),
                asunto,
                "RABBITMQ_EVENT",
                LocalDateTime.now(),
                "Correo de confirmación de orden despachado exitosamente"
        );
        historial.add(0, response);
        return response;
    }

    /**
     * Procesa y despacha una notificación enviada directamente vía petición HTTP (ej: Postman).
     */
    public NotificacionResponseDTO enviarNotificacionDirecta(NotificacionRequestDTO request) {
        String messageId = "NOTIF-POSTMAN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        logEmailBanner(messageId, request.getDestinatario(), request.getAsunto(), request.getMensaje(), "POSTMAN_DIRECT_API");

        NotificacionResponseDTO response = new NotificacionResponseDTO(
                "SENT",
                messageId,
                request.getDestinatario(),
                request.getAsunto(),
                "POSTMAN_DIRECT_API",
                LocalDateTime.now(),
                "Notificación manual enviada exitosamente vía API REST"
        );
        historial.add(0, response);
        return response;
    }

    public List<NotificacionResponseDTO> obtenerHistorial() {
        return new ArrayList<>(historial);
    }

    private void logEmailBanner(String id, String to, String subject, String body, String source) {
        log.info("==================================================================");
        log.info(">>> [SMTP DISPATCH SIMULATOR] Correo Electrónico Enviado con Éxito");
        log.info("ID Mensaje   : {}", id);
        log.info("Origen       : {}", source);
        log.info("Remitente    : {}", remitente);
        log.info("Destinatario : {}", to);
        log.info("Asunto       : {}", subject);
        log.info("Cuerpo Mensaje:\n{}", body.trim());
        log.info("==================================================================");
    }
}
