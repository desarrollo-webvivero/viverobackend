package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.List;

// Todo lo que se muestra en la ventana "Ver detalle" de un pedido
public class PedidoDetalleResponse {

    public record Producto(String nombre, Integer cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {}

    private Long id;
    private String fecha;
    private String estado;
    private String metodo;
    private BigDecimal subtotal;
    private BigDecimal costoEnvio;
    private BigDecimal total;
    private String departamento;
    private String municipio;
    private String zonaReferencia;
    private String direccionEntrega;
    private List<Producto> productos;

    public PedidoDetalleResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getCostoEnvio() { return costoEnvio; }
    public void setCostoEnvio(BigDecimal costoEnvio) { this.costoEnvio = costoEnvio; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }

    public String getZonaReferencia() { return zonaReferencia; }
    public void setZonaReferencia(String zonaReferencia) { this.zonaReferencia = zonaReferencia; }

    public String getDireccionEntrega() { return direccionEntrega; }
    public void setDireccionEntrega(String direccionEntrega) { this.direccionEntrega = direccionEntrega; }

    public List<Producto> getProductos() { return productos; }
    public void setProductos(List<Producto> productos) { this.productos = productos; }
}
