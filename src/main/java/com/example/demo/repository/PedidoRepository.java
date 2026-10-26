package com.example.demo.repository;

import com.example.demo.model.Cotizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.List; //  panel de pedidos --change wilson

public interface PedidoRepository extends JpaRepository<Cotizacion, Long> { // O el nombre de tu modelo


    // Pedidos de un cliente, del más reciente al más antiguo--change wilson
    List<Cotizacion> findByCliente_IdOrderByFechaSolicitudDesc(Long clienteId);
    
    // Calcula la suma de todos los totales directamente en la base de datos
    @Query("SELECT SUM(c.totalEstimado) FROM Cotizacion c") // Ajusta 'totalEstimado' al nombre exacto de tu variable en el Model
    BigDecimal sumarTotalPedidos();
}
