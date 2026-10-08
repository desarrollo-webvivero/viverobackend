package com.example.demo.dto;

public class EstadoPedido {
    private String estado;

    public EstadoPedido() {}

    public EstadoPedido(String estado) {
        this.estado = estado;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}