package com.example.demo.service;

import com.example.demo.dto.DashboarResumenRequest;
import com.example.demo.dto.DashboarResumenRequest.PedidoResumenDTO;
import com.example.demo.dto.DashboarResumenRequest.PlantaResumenDTO;
import com.example.demo.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    // Declaramos los repositorios
    private final PedidoRepository pedidoRepository;
    // Si en el futuro creas PlantaRepository, desintermínalo aquí:
    // private final PlantaRepository plantaRepository;

    // Inyección de dependencias por constructor
    public DashboardService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public DashboarResumenRequest obtenerResumenCompras() {
        
        // 1. Va a la BD y cuenta cuántas filas (pedidos) existen
        long totalCompras = pedidoRepository.count(); 
        
        // 2. Va a la BD y suma los montos. Si la BD está vacía o da null, se asigna BigDecimal.ZERO
        BigDecimal totalMonto = pedidoRepository.sumarTotalPedidos();
        if (totalMonto == null) {
            totalMonto = BigDecimal.ZERO;
        }

        // 3. Inicializamos listas vacías para las tablas en lugar de usar null
        List<PlantaResumenDTO> plantasEnPeligro = new ArrayList<>();
        List<PedidoResumenDTO> ultimosPedidos = new ArrayList<>();

        // 4. Retornamos el DTO con los parámetros en el orden exacto definido en DashboarResumenRequest:
        // (ventasDelMes, ordenesDelMes, valorInventario, totalPlantasActivas, alertasStockBajo, plantasEnPeligro, ultimosPedidos)
        return new DashboarResumenRequest(
            totalMonto,         // ventasDelMes
            totalCompras,       // ordenesDelMes
            BigDecimal.ZERO,    // valorInventario
            0L,                 // totalPlantasActivas
            0L,                 // alertasStockBajo
            plantasEnPeligro,   // plantasEnPeligro
            ultimosPedidos      // ultimosPedidos
        );
    }
}