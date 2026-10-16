package com.example.demo.service;

public class EmailNotificador {
    
}
package com.example.demo.Service.notificacion;

import com.example.demo.model.CanalVerificacion;
import com.example.demo.model.Cliente;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificador implements Notificador {

    private final JavaMailSender mailSender;
    private final String remitente;

    public EmailNotificador(JavaMailSender mailSender,
                            @Value("${app.mail.from:}") String remitente) {
        this.mailSender = mailSender;
        this.remitente = remitente;
    }

    @Override
    public CanalVerificacion canal() {
        return CanalVerificacion.EMAIL;
    }

    @Override
    public void enviarCodigo(Cliente cliente, String codigo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(cliente.getCorreoElectronico());
        mensaje.setSubject("Tu código de verificación");
        mensaje.setText("Hola " + cliente.getNombreCompleto() + ",\n\n"
                + "Tu código de verificación es: " + codigo + "\n"
                + "Vence en 10 minutos. Si no fuiste tú, ignora este mensaje.");
        mailSender.send(mensaje);
    }
}
