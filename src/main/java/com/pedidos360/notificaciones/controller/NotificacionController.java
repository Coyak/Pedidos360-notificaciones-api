package com.pedidos360.notificaciones.controller;

import com.pedidos360.notificaciones.dto.NotificacionRequestDTO;
import com.pedidos360.notificaciones.dto.NotificacionResponseDTO;
import com.pedidos360.notificaciones.service.NotificacionEmailService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para el microservicio de Notificaciones.
 * Expone endpoints de diagnóstico, histórico y envío directo testeable desde Postman.
 */
@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionEmailService emailService;

    public NotificacionController(NotificacionEmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Endpoint público de estado del microservicio
     * GET /api/notificaciones/status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getStatus() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Microservicio de Notificaciones en ejecución");
        response.put("port", "8082");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint directo testeable desde Postman para disparar notificación por correo.
     * POST /api/notificaciones/enviar
     */
    @PostMapping("/enviar")
    public ResponseEntity<NotificacionResponseDTO> enviarNotificacionDirecta(
            @Valid @RequestBody NotificacionRequestDTO request) {
        NotificacionResponseDTO response = emailService.enviarNotificacionDirecta(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint de consulta para visualizar las notificaciones enviadas y su trazabilidad.
     * GET /api/notificaciones/historial
     */
    @GetMapping("/historial")
    public ResponseEntity<List<NotificacionResponseDTO>> obtenerHistorial() {
        return ResponseEntity.ok(emailService.obtenerHistorial());
    }
}
