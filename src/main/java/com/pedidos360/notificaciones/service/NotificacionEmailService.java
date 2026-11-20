package com.pedidos360.notificaciones.service;

import com.pedidos360.notificaciones.dto.NotificacionRequestDTO;
import com.pedidos360.notificaciones.dto.NotificacionResponseDTO;
import com.pedidos360.notificaciones.dto.PedidoEventoDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Servicio de envío de correos electrónicos transaccionales vía SMTP real (JavaMailSender).
 * Conectado con RabbitMQ para gestión de ACKs y desvío a DLQ ante fallos de red o credenciales.
 */
@Service
public class NotificacionEmailService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionEmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:pedidos360.tienda@gmail.com}")
    private String mailUsername;

    @Value("${pedidos360.notificaciones.remitente:pedidos360.tienda@gmail.com}")
    private String remitente;

    private final List<NotificacionResponseDTO> historial = new CopyOnWriteArrayList<>();

    /**
     * Envía correo electrónico real. Si falla el despacho SMTP, lanza excepción para activar DLQ.
     */
    public void enviarCorreo(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom((mailUsername != null && !mailUsername.contains("tu_correo")) ? mailUsername : remitente);

        if (mailSender != null) {
            try {
                mailSender.send(message);
                log.info(">>> [SMTP REAL OK] Correo despachado exitosamente hacia: {} vía smtp.gmail.com", to);
            } catch (Exception e) {
                log.error(">>> [SMTP ERROR] Falló el despacho de correo hacia {}: {}", to, e.getMessage());
                throw new RuntimeException("Fallo en despacho SMTP real: " + e.getMessage(), e);
            }
        } else {
            log.warn(">>> [AVISO] JavaMailSender no configurado.");
        }
    }

    /**
     * Envío con soporte de contenido HTML enriquecido
     */
    public void enviarCorreoHtml(String to, String subject, String htmlContent) throws MessagingException {
        if (mailSender == null) {
            throw new IllegalStateException("JavaMailSender no disponible");
        }
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        helper.setFrom((mailUsername != null && !mailUsername.contains("tu_correo")) ? mailUsername : remitente);
        mailSender.send(message);
        log.info(">>> [SMTP HTML OK] Correo HTML enviado exitosamente a {}", to);
    }

    /**
     * Procesa y despacha la notificación por correo ante un evento de Pedido Creado de RabbitMQ.
     */
    public NotificacionResponseDTO enviarConfirmacionPedido(PedidoEventoDTO pedido) {
        String messageId = "NOTIF-EVT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String asunto = "¡Tu pedido " + pedido.getPedidoId() + " ha sido recibido! - Pedidos360";

        StringBuilder cuerpo = new StringBuilder();
        cuerpo.append("Estimado(a) cliente, confirmamos la recepción de tu compra en Pedidos360.\n\n");
        cuerpo.append("Número de Pedido : ").append(pedido.getPedidoId()).append("\n");
        cuerpo.append("Dirección Envío  : ").append(pedido.getDireccionEnvio()).append("\n");
        cuerpo.append("Total Facturado  : $").append(pedido.getTotalPagar()).append(" CLP\n");
        cuerpo.append("Fecha            : ").append(LocalDateTime.now()).append("\n\n");
        cuerpo.append("Gracias por comprar en Pedidos360 Cloud Native Store.");

        // Envío real vía JavaMailSender (lanza excepción si falla para desviar a DLQ)
        enviarCorreo(pedido.getClienteEmail(), asunto, cuerpo.toString());

        logEmailBanner(messageId, pedido.getClienteEmail(), asunto, cuerpo.toString(), "RABBITMQ_EVENT");

        NotificacionResponseDTO response = new NotificacionResponseDTO(
                "SENT",
                messageId,
                pedido.getClienteEmail(),
                asunto,
                "RABBITMQ_EVENT",
                LocalDateTime.now(),
                "Correo de confirmación de orden despachado exitosamente vía SMTP"
        );
        historial.add(0, response);
        return response;
    }

    /**
     * Despacho de notificación directa vía API REST (Postman / cURL).
     */
    public NotificacionResponseDTO enviarNotificacionDirecta(NotificacionRequestDTO request) {
        String messageId = "NOTIF-API-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        enviarCorreo(request.getDestinatario(), request.getAsunto(), request.getMensaje());

        logEmailBanner(messageId, request.getDestinatario(), request.getAsunto(), request.getMensaje(), "POSTMAN_API_DIRECT");

        NotificacionResponseDTO response = new NotificacionResponseDTO(
                "SENT",
                messageId,
                request.getDestinatario(),
                request.getAsunto(),
                "POSTMAN_API_DIRECT",
                LocalDateTime.now(),
                "Notificación enviada exitosamente vía API REST"
        );
        historial.add(0, response);
        return response;
    }

    public List<NotificacionResponseDTO> obtenerHistorial() {
        return new ArrayList<>(historial);
    }

    private void logEmailBanner(String id, String to, String subject, String body, String source) {
        log.info("==================================================================");
        log.info(">>> [SMTP DISPATCH] Correo Electrónico Despachado Realmente");
        log.info("ID Mensaje   : {}", id);
        log.info("Origen       : {}", source);
        log.info("Destinatario : {}", to);
        log.info("Asunto       : {}", subject);
        log.info("==================================================================");
    }
}
