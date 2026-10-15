package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "COTIZACIONES")
public class Cotizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_COTIZACION")
    private Long idCotizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CLIENTE", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_ENVIO", nullable = false)
    private Envio envio;

    @Column(name = "FECHA_SOLICITUD", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false)
    private EstadoPedido estado;

    @Column(name = "TOTAL_ESTIMADO", nullable = false)
    private BigDecimal totalEstimado;

    @Column(name = "METODO_PAGO", nullable = false)
    private String metodoPago;

    @PrePersist
    protected void prePersist() {

        if (fechaSolicitud == null) {
            fechaSolicitud = LocalDateTime.now();
        }

        if (estado == null) {
            estado = EstadoPedido.PENDIENTE;
        }
    }

    // getters y setters...


    public Cotizacion() {}

    public Long getIdCotizacion() { return idCotizacion; }
    public void setIdCotizacion(Long idCotizacion) { this.idCotizacion = idCotizacion; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Envio getEnvio() { return envio; }
    public void setEnvio(Envio envio) { this.envio = envio; }

    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }

    public BigDecimal getTotalEstimado() { return totalEstimado; }
    public void setTotalEstimado(BigDecimal totalEstimado) { this.totalEstimado = totalEstimado; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
}