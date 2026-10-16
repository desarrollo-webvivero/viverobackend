package com.example.demo.model;

public enum CanalVerificacion {
    EMAIL,
    WHATSAPP;

    /** Convierte el texto que llega del frontend. Devuelve null si no es válido. */
    public static CanalVerificacion desde(String texto) {
        if (texto == null) return null;
        try {
            return CanalVerificacion.valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
