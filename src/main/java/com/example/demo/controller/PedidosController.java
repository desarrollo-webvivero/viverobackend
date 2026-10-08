package com.example.demo.controller;

import com.example.demo.dto.EstadoPedido;
import com.example.demo.dto.PedidosRequest;
import com.example.demo.service.PedidosService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
// Permitimos que React (usualmente en puerto 3000 o Vite en 5173) se comunique con Spring Boot
@CrossOrigin(origins = "*") 
public class PedidosController {

    private final PedidosService pedidosService;

    public PedidosController(PedidosService pedidosService) {
        this.pedidosService = pedidosService;
    }

    // GET: /api/pedidos -> Devuelve la lista completa para poblar tu tabla en React
    @GetMapping
    public ResponseEntity<List<PedidosRequest>> obtenerPedidos() {
        List<PedidosRequest> pedidos = pedidosService.obtenerTodosLosPedidos();
        return ResponseEntity.ok(pedidos);
    }

    // PUT: /api/pedidos/{id}/estado -> Recibe la instrucción de cambio de estado
    @PutMapping("/{id}/estado")
    public ResponseEntity<String> cambiarEstadoPedido(@PathVariable Long id, @RequestBody EstadoPedido estadoDTO) {
        try {
            pedidosService.actualizarEstadoPedido(id, estadoDTO.getEstado());
            return ResponseEntity.ok("Estado actualizado correctamente a: " + estadoDTO.getEstado());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar estado: " + e.getMessage());
        }
    }
}