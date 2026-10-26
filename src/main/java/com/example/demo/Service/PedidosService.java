package com.example.demo.service;

import com.example.demo.dto.PedidosRequest;
import com.example.demo.model.EstadoPedido;
import com.example.demo.model.Cotizacion;
import com.example.demo.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PedidosService {

    private final PedidoRepository pedidosRepository;

    public PedidosService(PedidoRepository pedidosRepository) {
        this.pedidosRepository = pedidosRepository;
    }

    // Todos los pedidos (para el panel de administración)
    public List<PedidosRequest> obtenerTodosLosPedidos() {
        return pedidosRepository.findAll().stream()
            .map(this::aDto)
            .collect(Collectors.toList());
    }

    // Solo los pedidos del cliente indicado (para la página "Mi cuenta")
    public List<PedidosRequest> obtenerPedidosPorCliente(Long clienteId) {
        return pedidosRepository.findByCliente_IdOrderByFechaSolicitudDesc(clienteId).stream()
            .map(this::aDto)
            .collect(Collectors.toList());
    }

    // Convierte una Cotizacion de la base de datos al formato que recibe el frontend
    private PedidosRequest aDto(Cotizacion pedido) {
        return new PedidosRequest(
            pedido.getIdCotizacion(),
            pedido.getCliente() != null ? pedido.getCliente().getNombreCompleto() : "Cliente Desconocido",
            pedido.getFechaSolicitud() != null ? pedido.getFechaSolicitud().toString() : "N/A",
            pedido.getTotalEstimado(),
            pedido.getEstado() != null ? pedido.getEstado().name() : "PENDIENTE",
            pedido.getMetodoPago()
        );
    }

    public Cotizacion actualizarEstadoPedido(Long id, EstadoPedido nuevoEstado) {
        Cotizacion pedido = pedidosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));

        pedido.setEstado(nuevoEstado);
        return pedidosRepository.save(pedido);
    }
}
