package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "CLIENTES")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CLIENTE")
    private Long id;

    @Column(name = "NOMBRE_COMPLETO", nullable = false, length = 150)
    private String nombreCompleto;

    @Column(name = "TELEFONO", length = 20)
    private String telefono;

    @Column(name = "CORREO_ELECTRONICO", nullable = false, unique = true, length = 150)
    private String correoElectronico;

    @Column(name = "CONTRASENA_HASH", nullable = false, length = 255)
    private String contrasenaHash;

    @Column(name = "CUENTA_VERIFICADA", nullable = false)
    private Integer cuentaVerificada = 0;

    // Aquí se guarda el HASH del código de 6 dígitos (no el código en claro).
    @Column(name = "TOKEN_VERIFICACION", length = 100)
    private String tokenVerificacion;

    // NUEVO: hasta cuándo es válido el código.
    @Column(name = "TOKEN_EXPIRA")
    private LocalDateTime tokenExpira;

    // NUEVO: intentos fallidos con el código actual.
    @Column(name = "TOKEN_INTENTOS", nullable = false)
    private Integer tokenIntentos = 0;

    public Cliente() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreoElectronico() { return correoElectronico; }
    public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }

    public String getContrasenaHash() { return contrasenaHash; }
    public void setContrasenaHash(String contrasenaHash) { this.contrasenaHash = contrasenaHash; }

    public Integer getCuentaVerificada() { return cuentaVerificada; }
    public void setCuentaVerificada(Integer cuentaVerificada) { this.cuentaVerificada = cuentaVerificada; }

    public String getTokenVerificacion() { return tokenVerificacion; }
    public void setTokenVerificacion(String tokenVerificacion) { this.tokenVerificacion = tokenVerificacion; }

    public LocalDateTime getTokenExpira() { return tokenExpira; }
    public void setTokenExpira(LocalDateTime tokenExpira) { this.tokenExpira = tokenExpira; }

    public Integer getTokenIntentos() { return tokenIntentos; }
    public void setTokenIntentos(Integer tokenIntentos) { this.tokenIntentos = tokenIntentos; }
}
