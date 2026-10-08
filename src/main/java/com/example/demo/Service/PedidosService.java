package com.example.demo.service;

import com.example.demo.dto.PedidosRequest;
import com.example.demo.model.EstadoPedido;
import com.example.demo.model.Pedidos;
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

    public List<PedidosRequest> obtenerTodosLosPedidos() {
        List<Pedidos> pedidos = pedidosRepository.findAll();

        return pedidos.stream().map(pedido -> new PedidosRequest(
            pedido.getIdCotizacion(),
            pedido.getCliente() != null ? pedido.getCliente().getNombreCompleto() : "Cliente Desconocido",
            pedido.getFechaSolicitud() != null ? pedido.getFechaSolicitud().toString() : "N/A",
            pedido.getTotalEstimado(),
            pedido.getEstado() != null ? pedido.getEstado().name() : "PENDIENTE",
            pedido.getMetodoPago()
        )).collect(Collectors.toList());
    }

    public Pedidos actualizarEstadoPedido(Long id, EstadoPedido nuevoEstado) {
        Pedidos pedido = pedidosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));

        pedido.setEstado(nuevoEstado);
        return pedidosRepository.save(pedido);
    }
}