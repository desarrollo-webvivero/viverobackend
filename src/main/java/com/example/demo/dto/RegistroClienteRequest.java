package com.example.demo.dto;

public class RegistroClienteRequest {

    private String nombreCompleto;
    private String telefono;
    private String correoElectronico;
    private String contrasena;
    // NUEVO: "EMAIL" o "WHATSAPP"
    private String canalVerificacion;

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreoElectronico() { return correoElectronico; }
    public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getCanalVerificacion() { return canalVerificacion; }
    public void setCanalVerificacion(String canalVerificacion) { this.canalVerificacion = canalVerificacion; }
}
