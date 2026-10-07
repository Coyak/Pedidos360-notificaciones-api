package com.pedidos360.notificaciones.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class NotificacionRequestDTO {

    @NotBlank(message = "El email del destinatario es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String destinatario;

    @NotBlank(message = "El asunto no puede estar vacío")
    @Size(max = 150, message = "El asunto no puede exceder los 150 caracteres")
    private String asunto;

    @NotBlank(message = "El mensaje o cuerpo de la notificación no puede estar vacío")
    private String mensaje;

    public NotificacionRequestDTO() {}

    public NotificacionRequestDTO(String destinatario, String asunto, String mensaje) {
        this.destinatario = destinatario;
        this.asunto = asunto;
        this.mensaje = mensaje;
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

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
