package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "CLIENTES")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CLIENTE")
    private Long id;

    @Column(name = "NOMBRE_COMPLETO", nullable = false, length = 150)
    private String nombre;

    @Column(name = "TELEFONO", length = 20)
    private String telefono;

    @Column(name = "CORREO_ELECTRONICO", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "CONTRASENA_HASH", nullable = false, length = 255)
    private String contrasenaHash;

    @Column(name = "CUENTA_VERIFICADA", nullable = false)
    private Integer cuentaVerificada = 0;

    @Column(name = "TOKEN_VERIFICACION", length = 100)
    private String tokenVerificacion;

    public Usuario() {}

    public Usuario(String nombre, String email, String contrasenaHash) {
        this.nombre = nombre;
        this.email = email;
        this.contrasenaHash = contrasenaHash;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContrasenaHash() { return contrasenaHash; }
    public void setContrasenaHash(String contrasenaHash) { this.contrasenaHash = contrasenaHash; }

    public Integer getCuentaVerificada() { return cuentaVerificada; }
    public void setCuentaVerificada(Integer cuentaVerificada) { this.cuentaVerificada = cuentaVerificada; }

    public String getTokenVerificacion() { return tokenVerificacion; }
    public void setTokenVerificacion(String tokenVerificacion) { this.tokenVerificacion = tokenVerificacion; }
}