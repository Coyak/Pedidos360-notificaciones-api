package com.pedidos360.notificaciones.controller;

import com.pedidos360.notificaciones.dto.NotificacionRequestDTO;
import com.pedidos360.notificaciones.dto.NotificacionResponseDTO;
import com.pedidos360.notificaciones.service.NotificacionEmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para el microservicio de Notificaciones.
 * Soporta endpoints bajo /api/notificaciones y /api/email para compatibilidad total con la guía docente.
 */
@RestController
@RequestMapping({"/api/notificaciones", "/api/email"})
public class NotificacionController {

    private final NotificacionEmailService emailService;

    public NotificacionController(NotificacionEmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Endpoint público de estado del microservicio
     * GET /api/notificaciones/status o GET /api/email/status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getStatus() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Microservicio de Notificaciones Pedidos360 en ejecución");
        response.put("port", "8082");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint compatible con la guía del profesor (form-data / x-www-form-urlencoded / cURL --form)
     * POST /api/notificaciones/enviar o POST /api/email/enviar
     */
    @PostMapping(
            value = "/enviar",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE}
    )
    public ResponseEntity<String> enviarCorreoPrueba(
            @RequestParam("to") String to,
            @RequestParam("subject") String subject,
            @RequestParam("body") String body) {
        try {
            // Debe llamarse igual que en el servicio ("enviarCorreo")
            emailService.enviarCorreo(to, subject, body);

            // Registrar en historial para trazabilidad
            NotificacionRequestDTO notifDto = new NotificacionRequestDTO(to, subject, body);
            emailService.enviarNotificacionDirecta(notifDto);

            return ResponseEntity.ok("¡Correo enviado exitosamente a " + to + "!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al enviar el correo: " + e.getMessage());
        }
    }

    /**
     * Endpoint compatible con JSON REST tradicional (Postman JSON body)
     * POST /api/notificaciones/enviar o POST /api/email/enviar
     */
    @PostMapping(value = "/enviar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> enviarNotificacionJson(
            @RequestBody NotificacionRequestDTO request) {
        try {
            if (request.getDestinatario() == null || request.getAsunto() == null || request.getMensaje() == null) {
                return ResponseEntity.badRequest().body("Campos 'destinatario', 'asunto' y 'mensaje' son obligatorios en el JSON.");
            }
            emailService.enviarCorreo(request.getDestinatario(), request.getAsunto(), request.getMensaje());
            NotificacionResponseDTO response = emailService.enviarNotificacionDirecta(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al enviar el correo: " + e.getMessage());
        }
    }

    /**
     * Endpoint de consulta para visualizar las notificaciones enviadas y su trazabilidad.
     * GET /api/notificaciones/historial o GET /api/email/historial
     */
    @GetMapping("/historial")
    public ResponseEntity<List<NotificacionResponseDTO>> obtenerHistorial() {
        return ResponseEntity.ok(emailService.obtenerHistorial());
    }
}
