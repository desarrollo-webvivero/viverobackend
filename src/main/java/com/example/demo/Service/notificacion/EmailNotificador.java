package com.example.demo.service.notificacion;

import com.example.demo.model.CanalVerificacion;
import com.example.demo.model.Cliente;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Envía el código por correo usando la API HTTP de Brevo (puerto 443).
 * Render (plan gratis) bloquea SMTP (25/465/587), por eso ya no usamos JavaMailSender.
 */
@Component
public class EmailNotificador implements Notificador {

    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    private final RestClient restClient;
    private final String apiKey;
    private final String remitenteEmail;
    private final String remitenteNombre;

    public EmailNotificador(@Value("${brevo.api.key:}") String apiKey,
                            @Value("${app.mail.from:}") String remitenteEmail,
                            @Value("${app.mail.from.name:Vivero El Pensamiento}") String remitenteNombre) {
        this.apiKey = apiKey;
        this.remitenteEmail = remitenteEmail;
        this.remitenteNombre = remitenteNombre;

        // Timeouts: si Brevo no responde, fallamos rápido en vez de quedarnos colgados.
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5_000);
        factory.setReadTimeout(10_000);
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    @Override
    public CanalVerificacion canal() {
        return CanalVerificacion.EMAIL;
    }

    @Override
    public void enviarCodigo(Cliente cliente, String codigo) {
        if (apiKey.isBlank()) {
            throw new IllegalStateException("Falta la variable BREVO_API_KEY.");
        }
        if (remitenteEmail.isBlank()) {
            throw new IllegalStateException("Falta la variable MAIL_FROM (correo remitente verificado en Brevo).");
        }

        Map<String, Object> cuerpo = Map.of(
                "sender", Map.of("name", remitenteNombre, "email", remitenteEmail),
                "to", List.of(Map.of("email", cliente.getCorreoElectronico(),
                                     "name", cliente.getNombreCompleto())),
                "subject", "Tu código de verificación",
                "textContent", "Hola " + cliente.getNombreCompleto() + ",\n\n"
                        + "Tu código de verificación es: " + codigo + "\n"
                        + "Vence en 10 minutos. Si no fuiste tú, ignora este mensaje."
        );

        // retrieve() lanza excepción si Brevo responde 4xx/5xx (clave inválida, remitente no verificado, etc.)
        restClient.post()
                .uri(BREVO_URL)
                .header("api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(cuerpo)
                .retrieve()
                .toBodilessEntity();
    }
}