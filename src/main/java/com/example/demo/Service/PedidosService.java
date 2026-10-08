package com.example.demo.service;

import com.example.demo.dto.PedidosRequest;
import com.example.demo.model.EstadoPedido; // Ajusta este import según la ubicación de tu Enum
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
          pedido.getCliente() != null ? pedido.getCliente().getNombreCompleto() : "Cliente Desconocido",
          pedido.getFechaSolicitud() != null ? pedido.getFechaSolicitud().toString() : "N/A",
          pedido.getTotalEstimado(),
          pedido.getEstado() != null ? pedido.getEstado().toString() : "PENDIENTE",
          pedido.getMetodoPago()
        )).collect(Collectors.toList());
    }

    // Método para que el administrador cambie el estado (Acepta EstadoPedido y retorna Pedidos)
    public Pedidos actualizarEstadoPedido(Long id, EstadoPedido nuevoEstado) {
        Pedidos pedido = pedidosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));

        // Si en tu entidad Pedidos el atributo 'estado' es String, usa: pedido.setEstado(nuevoEstado.name());
        pedido.setEstado(nuevoEstado);
        return pedidosRepository.save(pedido);
    }
}