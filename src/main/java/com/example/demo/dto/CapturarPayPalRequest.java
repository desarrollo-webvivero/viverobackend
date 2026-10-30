package com.example.demo.dto;


public class CapturarPayPalRequest {

    private String paypalOrderId;          
    private CrearCotizacionRequest pedido;  

    public CapturarPayPalRequest() {
    }

    public String getPaypalOrderId() { return paypalOrderId; }
    public void setPaypalOrderId(String paypalOrderId) { this.paypalOrderId = paypalOrderId; }

    public CrearCotizacionRequest getPedido() { return pedido; }
    public void setPedido(CrearCotizacionRequest pedido) { this.pedido = pedido; }
}
