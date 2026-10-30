package com.example.demo.controller;

import com.example.demo.dto.CapturarPayPalRequest;
import com.example.demo.dto.CrearCotizacionRequest;
import com.example.demo.service.PayPalService;
import com.example.demo.service.PedidosService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

// Flujo de PayPal:
// 1) POST /api/paypal/orden    -> calcula el total, lo pasa a USD y crea la orden en PayPal
// 2) el cliente aprueba en la ventana de PayPal
// 3) POST /api/paypal/capturar -> cobra la orden y, si se completó, guarda el pedido
@RestController
@RequestMapping("/api/paypal")
@CrossOrigin(origins = "*")
public class PayPalController {

    private final PayPalService payPalService;
    private final PedidosService pedidosService;

    public PayPalController(PayPalService payPalService, PedidosService pedidosService) {
        this.payPalService = payPalService;
        this.pedidosService = pedidosService;
    }

    // Para que el frontend muestre el equivalente en dólares
    @GetMapping("/config")
    public Map<String, Object> config() {
        return Map.of(
            "configurado", payPalService.estaConfigurado(),
            "tasaCambio", payPalService.getTasaCambio(),
            "moneda", "USD"
        );
    }

    @PostMapping("/orden")
    public ResponseEntity<?> crearOrden(@RequestBody CrearCotizacionRequest pedido) {
        try {
            var calculado = pedidosService.calcularPedido(pedido);
            BigDecimal dolares = payPalService.aDolares(calculado.total());
            String orderId = payPalService.crearOrden(dolares, "Pedido Vivero El Pensamiento");

            return ResponseEntity.ok(Map.of(
                "id", orderId,
                "totalQuetzales", calculado.total(),
                "totalDolares", dolares
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("mensaje", "No pudimos iniciar el pago con PayPal."));
        }
    }

    @PostMapping("/capturar")
    public ResponseEntity<?> capturar(@RequestBody CapturarPayPalRequest request) {
        if (request.getPaypalOrderId() == null || request.getPedido() == null) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Faltan datos del pago."));
        }

        PedidosService.PedidoCalculado calculado;
        BigDecimal esperado;
        try {
            // Revalidamos ANTES de cobrar (stock, cliente, envío) para no cobrar un pedido inválido
            calculado = pedidosService.calcularPedido(request.getPedido());
            esperado = payPalService.aDolares(calculado.total());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }

        BigDecimal cobrado;
        try {
            // La orden debe ser por el mismo monto que este pedido (evita pagar un carrito y registrar otro)
            BigDecimal montoOrden = payPalService.montoDeOrden(request.getPaypalOrderId());
            if (montoOrden.compareTo(esperado) != 0) {
                return ResponseEntity.badRequest().body(Map.of(
                    "mensaje", "Tu carrito cambió mientras pagabas. No se hizo ningún cobro; intenta de nuevo."));
            }
            cobrado = payPalService.capturarOrden(request.getPaypalOrderId());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }

        try {
            if (cobrado.compareTo(esperado) != 0) {
                System.err.println("PayPal " + request.getPaypalOrderId() + ": se cobró " + cobrado + " USD pero se esperaba " + esperado);
            }
            var pedido = pedidosService.guardarPedido(calculado, "PAYPAL");
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("mensaje", "Pago completado", "pedido", pedido));
        } catch (Exception e) {
            // El dinero ya se cobró: dejamos registro para resolverlo a mano
            System.err.println("ATENCIÓN: pago PayPal " + request.getPaypalOrderId() + " cobrado pero el pedido no se guardó: " + e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "mensaje", "Tu pago se realizó, pero no pudimos registrar el pedido. Escríbenos por WhatsApp con este código: "
                    + request.getPaypalOrderId()
            ));
        }
    }
}
