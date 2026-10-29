package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

// Una fila por cada producto de un pedido (tabla DETALLE_COTIZACIONES)
@Entity
@Table(name = "DETALLE_COTIZACIONES")
public class DetalleCotizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DETALLE")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_COTIZACION", nullable = false)
    private Cotizacion cotizacion;

    // Id del producto; el nombre se busca en PRODUCTOS al mostrar el detalle.
    // El precio se guarda aquí para conservar el precio del momento de la compra.
    @Column(name = "ID_PRODUCTO")
    private Long productoId;

    @Column(name = "CANTIDAD", nullable = false)
    private Integer cantidad;

    @Column(name = "PRECIO_UNITARIO", nullable = false)
    private BigDecimal precioUnitario;

    public DetalleCotizacion() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cotizacion getCotizacion() { return cotizacion; }
    public void setCotizacion(Cotizacion cotizacion) { this.cotizacion = cotizacion; }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
}
