package com.example.demo.service.notificacion;

import com.example.demo.model.CanalVerificacion;
import com.example.demo.model.Cliente;

/**
 * Contrato: "algo que sabe enviar un código a un cliente".
 * EmailNotificador y WhatsAppNotificador cumplen este contrato.
 * Mañana podrías agregar SMS sin tocar el resto del código.
 */
public interface Notificador {
    CanalVerificacion canal();
    void enviarCodigo(Cliente cliente, String codigo);
}
