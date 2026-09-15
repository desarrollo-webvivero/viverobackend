package com.example.demo.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarCodigoVerificacion(String correoDestino, String codigo) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(correoDestino);
        helper.setSubject("Código de Verificación - Vivero Pensamiento 🌿");

        String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 500px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;'>"
                + "<h2 style='color: #2e5a44; text-align: center;'>¡Bienvenido a Vivero Pensamiento! 🌿</h2>"
                + "<p>Gracias por registrarte. Para verificar tu cuenta y continuar con tu compra, ingresa el siguiente código:</p>"
                + "<div style='background-color: #f4f8f5; padding: 15px; text-align: center; border-radius: 8px; margin: 20px 0;'>"
                + "<span style='font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #2e5a44;'>" + codigo + "</span>"
                + "</div>"
                + "<p style='font-size: 12px; color: #666; text-align: center;'>Este código vencerá en 15 minutos.</p>"
                + "</div>";

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
}