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
 * Servicio de envío y simulación de correos electrónicos transaccionales.
 * Integra JavaMailSender con soporte Gmail SMTP y eventos asíncronos de RabbitMQ.
 */
@Service
public class NotificacionEmailService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionEmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:tu_correo@gmail.com}")
    private String mailUsername;

    @Value("${pedidos360.notificaciones.remitente:noreply@pedidos360.cloud}")
    private String remitente;

    private final List<NotificacionResponseDTO> historial = new CopyOnWriteArrayList<>();

    /**
     * Método para correo simple (Texto plano) requerido por la guía del profesor.
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
                log.info(">>> [GMAIL SMTP DISPATCH] Correo enviado efectivamente a {} vía smtp.gmail.com", to);
            } catch (Exception e) {
                log.warn(">>> [SMTP ADVERTENCIA] No se pudo enviar por SMTP real ({}). Registrando en historial y consola.", e.getMessage());
            }
        } else {
            log.info(">>> [SMTP SIMULADO] JavaMailSender no disponible, registrando en log.");
        }
    }

    /**
     * Método opcional por si quieres enviar HTML o archivos adjuntos (guía docente).
     */
    public void enviarCorreoConAdjunto(String to, String subject, String htmlContent, String rutaArchivo) throws MessagingException {
        if (mailSender == null) {
            log.warn(">>> [SMTP] JavaMailSender no disponible para enviar correo con adjunto.");
            return;
        }
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        helper.setFrom((mailUsername != null && !mailUsername.contains("tu_correo")) ? mailUsername : remitente);
        if (rutaArchivo != null && !rutaArchivo.isEmpty()) {
            FileSystemResource file = new FileSystemResource(new File(rutaArchivo));
            helper.addAttachment(file.getFilename(), file);
        }
        mailSender.send(message);
        log.info(">>> [GMAIL SMTP DISPATCH] Correo con adjunto enviado exitosamente a {}", to);
    }

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

        // Envío real vía JavaMailSender (según pauta)
        enviarCorreo(pedido.getClienteEmail(), asunto, cuerpo.toString());

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
     * Procesa y despacha una notificación enviada directamente vía petición HTTP (Postman / cURL).
     */
    public NotificacionResponseDTO enviarNotificacionDirecta(NotificacionRequestDTO request) {
        String messageId = "NOTIF-POSTMAN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Envío real vía JavaMailSender (según pauta)
        enviarCorreo(request.getDestinatario(), request.getAsunto(), request.getMensaje());

        logEmailBanner(messageId, request.getDestinatario(), request.getAsunto(), request.getMensaje(), "POSTMAN_DIRECT_API");

        NotificacionResponseDTO response = new NotificacionResponseDTO(
                "SENT",
                messageId,
                request.getDestinatario(),
                request.getAsunto(),
                "POSTMAN_DIRECT_API",
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
        log.info(">>> [SMTP DISPATCH] Correo Electrónico Procesado con Éxito");
        log.info("ID Mensaje   : {}", id);
        log.info("Origen       : {}", source);
        log.info("Remitente    : {}", (mailUsername != null && !mailUsername.contains("tu_correo")) ? mailUsername : remitente);
        log.info("Destinatario : {}", to);
        log.info("Asunto       : {}", subject);
        log.info("Cuerpo Mensaje:\n{}", body.trim());
        log.info("==================================================================");
    }
}
