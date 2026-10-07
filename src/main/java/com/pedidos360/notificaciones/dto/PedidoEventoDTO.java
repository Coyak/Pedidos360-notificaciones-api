package com.pedidos360.notificaciones.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class PedidoEventoDTO implements Serializable {

    private String pedidoId;
    private String status;
    private String clienteEmail;
    private String direccionEnvio;
    private Double totalPagar;
    private LocalDateTime fechaCreacion;
    private List<ItemPedidoDTO> items;

    public PedidoEventoDTO() {}

    public PedidoEventoDTO(String pedidoId, String status, String clienteEmail, String direccionEnvio,
                           Double totalPagar, LocalDateTime fechaCreacion, List<ItemPedidoDTO> items) {
        this.pedidoId = pedidoId;
        this.status = status;
        this.clienteEmail = clienteEmail;
        this.direccionEnvio = direccionEnvio;
        this.totalPagar = totalPagar;
        this.fechaCreacion = fechaCreacion;
        this.items = items;
    }

    public String getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(String pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getClienteEmail() {
        return clienteEmail;
    }

    public void setClienteEmail(String clienteEmail) {
        this.clienteEmail = clienteEmail;
    }

    public String getDireccionEnvio() {
        return direccionEnvio;
    }

    public void setDireccionEnvio(String direccionEnvio) {
        this.direccionEnvio = direccionEnvio;
    }

    public Double getTotalPagar() {
        return totalPagar;
    }

    public void setTotalPagar(Double totalPagar) {
        this.totalPagar = totalPagar;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public List<ItemPedidoDTO> getItems() {
        return items;
    }

    public void setItems(List<ItemPedidoDTO> items) {
        this.items = items;
    }
}
