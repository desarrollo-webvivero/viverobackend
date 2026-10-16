package com.example.demo.service.notificacion;

import com.example.demo.model.CanalVerificacion;
import com.example.demo.model.Cliente;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * Envía el código por WhatsApp usando la API REST de Twilio.
 * (No necesita librería extra: solo una llamada HTTP.)
 */
@Component
public class WhatsAppNotificador implements Notificador {

    private final RestClient restClient = RestClient.create();
    private final String accountSid;
    private final String authToken;
    private final String desde;

    public WhatsAppNotificador(@Value("${twilio.account.sid:}") String accountSid,
                               @Value("${twilio.auth.token:}") String authToken,
                               @Value("${twilio.whatsapp.from:}") String desde) {
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.desde = desde;
    }

    @Override
    public CanalVerificacion canal() {
        return CanalVerificacion.WHATSAPP;
    }

    @Override
    public void enviarCodigo(Cliente cliente, String codigo) {
        if (accountSid.isBlank() || authToken.isBlank()) {
            throw new IllegalStateException("Twilio no está configurado (faltan variables de entorno).");
        }
        if (cliente.getTelefono() == null || cliente.getTelefono().isBlank()) {
            throw new IllegalStateException("El cliente no tiene teléfono registrado.");
        }

        MultiValueMap<String, String> formulario = new LinkedMultiValueMap<>();
        formulario.add("From", desde);
        formulario.add("To", "whatsapp:" + cliente.getTelefono());
        formulario.add("Body", "Tu código de verificación es: " + codigo
                + ". Vence en 10 minutos. Si no fuiste tú, ignora este mensaje.");

        restClient.post()
                .uri("https://api.twilio.com/2010-04-01/Accounts/{sid}/Messages.json", accountSid)
                .headers(h -> h.setBasicAuth(accountSid, authToken))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formulario)
                .retrieve()
                .toBodilessEntity();
    }
}
