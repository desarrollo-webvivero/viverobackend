package com.example.demo.dto;

// Un "record" es una clase corta e inmutable: Java crea solo los getters.
public record VerificarCuentaRequest(String correoElectronico, String codigo) {}
