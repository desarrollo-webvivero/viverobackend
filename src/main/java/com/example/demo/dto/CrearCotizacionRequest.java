package com.example.demo.dto;

import java.util.List;

public class CrearCotizacionRequest {

    private Long clienteId;
    private Long envioId;
    private String metodoPago;
    private String direccionEntrega;

    private List<DetalleCotizacionRequest> productos;

    public CrearCotizacionRequest() {
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getEnvioId() {
        return envioId;
    }

    public void setEnvioId(Long envioId) {
        this.envioId = envioId;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getDireccionEntrega() {
        return direccionEntrega;
    }

    public void setDireccionEntrega(String direccionEntrega) {
        this.direccionEntrega = direccionEntrega;
    }

    public List<DetalleCotizacionRequest> getProductos() {
        return productos;
    }

    public void setProductos(
            List<DetalleCotizacionRequest> productos) {

        this.productos = productos;
    }
}