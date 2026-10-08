package com.example.demo.controller;

import com.example.demo.model.EstadoPedido;
import com.example.demo.service.PedidosService;
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
            return ResponseEntity.badRequest().body("Error al actualizar el estado: " + e.getMessage());
        }
    }
}