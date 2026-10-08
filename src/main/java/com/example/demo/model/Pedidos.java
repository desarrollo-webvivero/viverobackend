package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity 
@Table(name = "Cotizaciones")
public class Pedidos {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Cotizacion")
    private Long idCotizacion;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_Cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_Envio", nullable = false)
    private Envio envio;

    @Column(name = "Fecha_Solicitud", nullable = false, updatable = false)
    private LocalDateTime fechaSolicitud;

    @Column (name = "Estado", nullable = false)
    private String estado;

    @Column (name = "Total_Estimado", nullable = false)
    private BigDecimal totalEstimado;

    @Column (name = "metodo_pago", nullable = false)
    private String metodoPago;

    public Pedidos() {}

    public Long getIdCotizacion() { return idCotizacion; }
    public void setIdCotizacion(Long idCotizacion) { this.idCotizacion = idCotizacion; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Envio getEnvio() { return envio; }
    public void setEnvio(Envio envio) { this.envio = envio; }

    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public BigDecimal getTotalEstimado() { return totalEstimado; }
    public void setTotalEstimado(BigDecimal totalEstimado) { this.totalEstimado = totalEstimado; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }


    



    



    




}

