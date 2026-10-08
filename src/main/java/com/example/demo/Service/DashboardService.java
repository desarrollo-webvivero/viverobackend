package com.example.demo.service;

import com.example.demo.dto.DashboarResumenRequest;
import com.example.demo.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class DashboardService {

    // Declaramos el repositorio
    private final PedidoRepository pedidoRepository;

    // Lo inyectamos a través del constructor
    public DashboardService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }
    public DashboarResumenRequest obtenerResumenCompras() {
        
        // 1. Va a la BD y cuenta cuántas filas (pedidos) existen
        long totalCompras = pedidoRepository.count(); 
        
        // 2. Va a la BD y suma los montos. Si la base de datos está vacía, devuelve 0 para evitar errores
        BigDecimal totalMonto = pedidoRepository.sumarTotalPedidos();
        if (totalMonto == null) {
            totalMonto = BigDecimal.ZERO;
        }

        // 3. Empaqueta la información y la envía al Controller
        return new DashboarResumenRequest(totalCompras, totalMonto);
    }
    
    // ... (el método va aquí abajo)
}