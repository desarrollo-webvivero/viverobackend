//code by wilson


package com.example.demo.dto;

import java.util.List;

// Lo que manda el frontend al confirmar un pedido (POST /api/pedidos)
public class CrearCotizacionRequest {

    private Long clienteId;
    private Long envioId;          // opcional: si no viene, se busca por departamento/municipio
    private String departamento;
    private String municipio;
    private String metodoPago;     // "CONTRA_ENTREGA", "WHATSAPP", "PAYPAL"
    private String direccionEntrega;

    private List<DetalleCotizacionRequest> productos;

    public CrearCotizacionRequest() {
    }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public Long getEnvioId() { return envioId; }
    public void setEnvioId(Long envioId) { this.envioId = envioId; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getDireccionEntrega() { return direccionEntrega; }
    public void setDireccionEntrega(String direccionEntrega) { this.direccionEntrega = direccionEntrega; }

    public List<DetalleCotizacionRequest> getProductos() { return productos; }
    public void setProductos(List<DetalleCotizacionRequest> productos) { this.productos = productos; }
}



// original one 
/*package com.example.demo.dto;

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
}*/
