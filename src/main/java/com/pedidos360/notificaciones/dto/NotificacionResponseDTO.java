package com.pedidos360.notificaciones.dto;

import java.time.LocalDateTime;

public class NotificacionResponseDTO {

    private String status;
    private String messageId;
    private String destinatario;
    private String asunto;
    private String origen;
    private LocalDateTime fechaEnvio;
    private String detalle;

    public NotificacionResponseDTO() {}

    public NotificacionResponseDTO(String status, String messageId, String destinatario,
                                   String asunto, String origen, LocalDateTime fechaEnvio, String detalle) {
        this.status = status;
        this.messageId = messageId;
        this.destinatario = destinatario;
        this.asunto = asunto;
        this.origen = origen;
        this.fechaEnvio = fechaEnvio;
        this.detalle = detalle;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }
}
