package com.example.demo.service;

import com.example.demo.dto.DashboarResumenRequest;
import com.example.demo.dto.DashboarResumenRequest.PedidoResumenDTO;
import com.example.demo.dto.DashboarResumenRequest.PlantaResumenDTO;
import com.example.demo.model.Producto;
import com.example.demo.repository.PedidoRepository;
import com.example.demo.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;

    public DashboardService(PedidoRepository pedidoRepository, ProductoRepository productoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
    }

    public DashboarResumenRequest obtenerResumenCompras() {
        
        // 1. Métricas de ventas
        long totalCompras = pedidoRepository.count(); 
        BigDecimal totalMonto = pedidoRepository.sumarTotalPedidos();
        if (totalMonto == null) {
            totalMonto = BigDecimal.ZERO;
        }

        // 2. Métricas del inventario (usando ProductoRepository)
        long totalProductosActivos = productoRepository.count();
        
        BigDecimal valorInventario = productoRepository.calcularValorTotalInventario();
        if (valorInventario == null) {
            valorInventario = BigDecimal.ZERO;
        }

        long alertasStockBajo = productoRepository.countByStockDisponibleLessThanEqual(5);

        // 3. Productos críticos por agotarse (stock <= 5)
        List<Producto> productosCriticos = productoRepository.findTop5ByStockDisponibleLessThanEqualOrderByStockDisponibleAsc(5);
        
        List<PlantaResumenDTO> plantasEnPeligro = productosCriticos.stream()
            .map(p -> new PlantaResumenDTO(
                p.getId(),
                p.getNombre(),
                p.getImagenUrl(),
                p.getStockDisponible()
            ))
            .collect(Collectors.toList());

        // 4. Lista para últimos pedidos
        List<PedidoResumenDTO> ultimosPedidos = new ArrayList<>();

        // 5. Retornar DTO con la información compilada
        return new DashboarResumenRequest(
            totalMonto,            // ventasDelMes
            totalCompras,          // ordenesDelMes
            valorInventario,       // valorInventario
            totalProductosActivos, // totalPlantasActivas
            alertasStockBajo,      // alertasStockBajo
            plantasEnPeligro,      // productos por agotarse
            ultimosPedidos         // últimos pedidos
        );
    }
}