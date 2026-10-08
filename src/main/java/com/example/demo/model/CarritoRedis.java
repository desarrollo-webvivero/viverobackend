package com.example.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import java.io.Serializable;
import java.math.BigDecimal;

// Guarda el carrito en memoria por un tiempo (ej. "carritos")
@RedisHash("carritos")
public class CarritoRedis implements Serializable { // Serializable permite guardar el objeto en RAM

    @Id
    private String idSesionUsuario; // ID único del visitante (ej. token)
    private Long idProducto;
    private Integer cantidad;
    private BigDecimal subtotal;

    // Constructores, Getters y Setters...

    public CarritoRedis() {
        // Default constructor
    }   

    public CarritoRedis(String idSesionUsuario, Long idProducto, Integer cantidad, BigDecimal subtotal) {
        this.idSesionUsuario = idSesionUsuario;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.subtotal = subtotal;
    }

    public String getIdSesionUsuario() {
        return idSesionUsuario;
    }

    public void setIdSesionUsuario(String idSesionUsuario) {
        this.idSesionUsuario = idSesionUsuario;
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto = idProducto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
}