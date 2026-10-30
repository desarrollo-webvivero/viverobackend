package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class PayPalService {

    private final String clientId;
    private final String clientSecret;
    private final BigDecimal tasaCambio; // quetzales por 1 dólar
    private final RestClient http;

    private String tokenActual;
    private Instant tokenVence = Instant.EPOCH;

    public PayPalService(
            @Value("${paypal.client.id:}") String clientId,
            @Value("${paypal.client.secret:}") String clientSecret,
            @Value("${paypal.base-url:https://api-m.sandbox.paypal.com}") String baseUrl,
            @Value("${paypal.tasa-cambio:7.70}") BigDecimal tasaCambio) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tasaCambio = tasaCambio;
        this.http = RestClient.builder().baseUrl(baseUrl).build();
    }

    public boolean estaConfigurado() {
        return !clientId.isBlank() && !clientSecret.isBlank();
    }


    public String getClientId() {
        return clientId;
    }

    public BigDecimal getTasaCambio() {
        return tasaCambio;
    }

    // Q -> USD con 2 decimales (PayPal no acepta quetzales)
    public BigDecimal aDolares(BigDecimal quetzales) {
        return quetzales.divide(tasaCambio, 2, RoundingMode.HALF_UP);
    }

    // Crea la orden en PayPal y devuelve su id
    public String crearOrden(BigDecimal dolares, String descripcion) {
        Map<String, Object> cuerpo = Map.of(
            "intent", "CAPTURE",
            "purchase_units", List.of(Map.of(
                "description", descripcion,
                "amount", Map.of("currency_code", "USD", "value", dolares.toPlainString())
            ))
        );

        Map<String, Object> respuesta = llamar("/v2/checkout/orders", cuerpo);
        Object id = respuesta.get("id");
        if (id == null) {
            throw new IllegalStateException("PayPal no devolvió el número de orden.");
        }
        return id.toString();
    }

    // Monto (USD) con el que se creó la orden en PayPal
    @SuppressWarnings("unchecked")
    public BigDecimal montoDeOrden(String orderId) {
        Map<String, Object> orden;
        try {
            orden = http.get()
                .uri("/v2/checkout/orders/" + orderId)
                .headers(h -> h.setBearerAuth(token()))
                .retrieve()
                .body(Map.class);
        } catch (RestClientResponseException e) {
            throw new IllegalStateException("No encontramos la orden de PayPal.");
        }
        try {
            var unidades = (List<Map<String, Object>>) orden.get("purchase_units");
            var monto = (Map<String, Object>) unidades.get(0).get("amount");
            return new BigDecimal(monto.get("value").toString());
        } catch (RuntimeException e) {
            throw new IllegalStateException("No pudimos leer la orden de PayPal.");
        }
    }

    // Cobra la orden aprobada. Devuelve el monto cobrado en USD (o lanza error si no se completó).
    @SuppressWarnings("unchecked")
    public BigDecimal capturarOrden(String orderId) {
        Map<String, Object> respuesta = llamar("/v2/checkout/orders/" + orderId + "/capture", Map.of());

        if (!"COMPLETED".equals(respuesta.get("status"))) {
            throw new IllegalStateException("PayPal no completó el pago (estado: " + respuesta.get("status") + ").");
        }

        try {
            var unidades = (List<Map<String, Object>>) respuesta.get("purchase_units");
            var pagos = (Map<String, Object>) unidades.get(0).get("payments");
            var capturas = (List<Map<String, Object>>) pagos.get("captures");
            var monto = (Map<String, Object>) capturas.get(0).get("amount");
            return new BigDecimal(monto.get("value").toString());
        } catch (RuntimeException e) {
            throw new IllegalStateException("No pudimos leer la respuesta de PayPal.");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> llamar(String ruta, Map<String, Object> cuerpo) {
        try {
            return http.post()
                .uri(ruta)
                .headers(h -> h.setBearerAuth(token()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(cuerpo)
                .retrieve()
                .body(Map.class);
        } catch (RestClientResponseException e) {
            System.err.println("Error de PayPal en " + ruta + ": " + e.getStatusCode() + " " + e.getResponseBodyAsString());
            throw new IllegalStateException("PayPal rechazó la operación. Intenta de nuevo.");
        }
    }

    // Token de acceso (se reutiliza hasta un minuto antes de que venza)
    @SuppressWarnings("unchecked")
    private synchronized String token() {
        if (!estaConfigurado()) {
            throw new IllegalStateException("PayPal no está configurado en el servidor.");
        }
        if (tokenActual != null && Instant.now().isBefore(tokenVence)) {
            return tokenActual;
        }
        try {
            Map<String, Object> respuesta = http.post()
                .uri("/v1/oauth2/token")
                .headers(h -> h.setBasicAuth(clientId, clientSecret))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("grant_type=client_credentials")
                .retrieve()
                .body(Map.class);

            tokenActual = respuesta.get("access_token").toString();
            long segundos = Long.parseLong(respuesta.getOrDefault("expires_in", 300).toString());
            tokenVence = Instant.now().plusSeconds(Math.max(segundos - 60, 30));
            return tokenActual;
        } catch (RestClientResponseException e) {
            System.err.println("Error al obtener token de PayPal: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
            throw new IllegalStateException("No pudimos conectar con PayPal. Revisa el Client ID y el Secret.");
        }
    }
}
