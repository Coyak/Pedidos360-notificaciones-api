package com.pedidos360.notificaciones.messaging;

import com.pedidos360.notificaciones.dto.PedidoEventoDTO;
import com.pedidos360.notificaciones.service.NotificacionEmailService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Consumidor de eventos de RabbitMQ para el dominio de Notificaciones por Correo.
 * Escucha la cola 'pedidos.notificaciones.queue', despacha el correo de confirmación
 * y gestiona ACK explícito manual o NACK hacia la DLQ en caso de fallo.
 */
@Component
public class NotificacionConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificacionConsumer.class);

    private final NotificacionEmailService emailService;

    public NotificacionConsumer(NotificacionEmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(
            queues = "${pedidos360.rabbitmq.queue.notificaciones}",
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void procesarNotificacionPedido(
            @Payload PedidoEventoDTO pedido,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {

        log.info("------------------------------------------------------------------");
        log.info(">>> [RABBITMQ NOTIFICACIONES] Recibido evento de pedido para notificar: {}", pedido.getPedidoId());

        try {
            if (pedido.getClienteEmail() == null || pedido.getClienteEmail().trim().isEmpty()) {
                log.error(">>> [ERROR VALIDACIÓN - DLQ] El pedido {} carece de email de cliente. Enviando a DLQ.", pedido.getPedidoId());
                channel.basicNack(deliveryTag, false, false);
                return;
            }

            // Despachar correo electrónico de confirmación
            emailService.enviarConfirmacionPedido(pedido);

            log.info(">>> [NOTIFICACIÓN OK] Correo emitido para pedido {}. Enviando ACK manual (deliveryTag={})",
                    pedido.getPedidoId(), deliveryTag);
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error(">>> [ERROR TÉCNICO - DLQ] Fallo al despachar notificación para pedido {}: {}. Enviando NACK hacia DLQ.",
                    pedido.getPedidoId(), e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false);
        } finally {
            log.info("------------------------------------------------------------------");
        }
    }
}
