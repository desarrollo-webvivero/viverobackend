package com.example.demo.dto;

import java.math.BigDecimal;

public class PedidosRequest {
    
    private Long id;
    private String cliente; // Enviaremos solo el Nombre_Completo
    private String fecha;
    private BigDecimal total;
    private String estado;
    private String metodo;

    public PedidosRequest(Long id, String cliente, String fecha, BigDecimal total, String estado, String metodo) {
        this.id = id;
        this.cliente = cliente;
        this.fecha = fecha;
        this.total = total;
        this.estado = estado;
        this.metodo = metodo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }


  
}
