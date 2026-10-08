package com.example.demo.model;
import jakarta.persistence.*;

@Entity
@Table(name = "Envios")
public class Envio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Envio")
    private Long id;

    @Column(name = "Departamento", nullable = false, length = 100)
    private String departamento;

    @Column(name = "Municipio", nullable = false, length = 100)
    private String municipio;

    @Column(name = "Zona_Referencia", length = 255)
    private String zonaReferencia;

    @Column(name = "Costo_Envio", nullable = false, precision = 10, scale = 2)
    private Double costoEnvio;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public String getZonaReferencia() {
        return zonaReferencia;
    }

    public void setZonaReferencia(String zonaReferencia) {
        this.zonaReferencia = zonaReferencia;
    }

    public Double getCostoEnvio() {
        return costoEnvio;
    }

    public void setCostoEnvio(Double costoEnvio) {
        this.costoEnvio = costoEnvio;
    }
    
}
