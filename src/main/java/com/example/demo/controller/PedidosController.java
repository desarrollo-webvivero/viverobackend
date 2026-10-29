package com.example.demo.controller;

import com.example.demo.dto.CrearCotizacionRequest;
import com.example.demo.model.EstadoPedido;
import com.example.demo.service.PedidosService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
public class PedidosController {

    private final PedidosService pedidosService;

    public PedidosController(PedidosService pedidosService) {
        this.pedidosService = pedidosService;
    }

    @GetMapping
    public ResponseEntity<?> obtenerPedidos() {
        try {
            var pedidos = pedidosService.obtenerTodosLosPedidos();
            return ResponseEntity.ok(pedidos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al obtener los pedidos: " + e.getMessage());
        }
    }

    
    @PostMapping
    public ResponseEntity<?> crearPedido(@RequestBody CrearCotizacionRequest request) {
        try {
            var pedido = pedidosService.crearPedido(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensaje", "Pedido registrado", "pedido", pedido));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("mensaje", "No pudimos registrar el pedido. Intenta de nuevo."));
        }
    }


    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<?> obtenerPedidosDeCliente(@PathVariable Long clienteId) {
        try {
            return ResponseEntity.ok(pedidosService.obtenerPedidosPorCliente(clienteId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al obtener los pedidos del cliente: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/detalle")
    public ResponseEntity<?> obtenerDetalle(@PathVariable Long id,
                                            @RequestParam(required = false) Long clienteId) {
        try {
            return ResponseEntity.ok(pedidosService.obtenerDetalle(id, clienteId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("mensaje", "No pudimos cargar el detalle del pedido."));
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstadoPedido(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String nuevoEstadoStr = body.get("estado");
            
            if (nuevoEstadoStr == null || nuevoEstadoStr.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El campo 'estado' es requerido.");
            }

            EstadoPedido nuevoEstado = EstadoPedido.valueOf(nuevoEstadoStr.trim().toUpperCase());
            
            var pedidoActualizado = pedidosService.actualizarEstadoPedido(id, nuevoEstado);
            return ResponseEntity.ok(pedidoActualizado);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Estado no válido. Use: PENDIENTE, ENVIADO, ENTREGADO o CANCELADO.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al actualizar el estado: " + e.getMessage());
        }
    }
}
