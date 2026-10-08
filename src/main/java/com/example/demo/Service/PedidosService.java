package com.example.demo.service;

import com.example.demo.dto.PedidosRequest;
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

    // Método para listar y formatear los datos para React
    public List<PedidosRequest> obtenerTodosLosPedidos() {
        List<Pedidos> pedidos = pedidosRepository.findAll();

        return pedidos.stream().map(pedido -> new PedidosRequest(
          pedido.getIdCotizacion(),
          pedido.getCliente().getNombreCompleto(), // Extraemos el String, no el objeto entero
          pedido.getFechaSolicitud() != null ? pedido.getFechaSolicitud().toString() : "N/A", // Convertimos a String
          pedido.getTotalEstimado(), // El total va antes que el estado, según tu DTO
          pedido.getEstado(),
          pedido.getMetodoPago()
        )).collect(Collectors.toList());
    }

    // Método para que el administrador cambie el estado (Enviado, Entregado, Cancelado)
    public void actualizarEstadoPedido(Long id, String nuevoEstado) {
        Pedidos pedido = pedidosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));

        // Aquí podrías agregar validaciones extra si lo deseas (ej. no cancelar si ya fue entregado)
        pedido.setEstado(nuevoEstado);

        pedidosRepository.save(pedido);
    }
}