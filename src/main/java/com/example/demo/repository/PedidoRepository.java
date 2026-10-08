package com.example.demo.repository;

import com.example.demo.model.Pedidos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;

public interface PedidoRepository extends JpaRepository<Pedidos, Long> { // O el nombre de tu modelo
    
    // Calcula la suma de todos los totales directamente en la base de datos
    @Query("SELECT SUM(p.totalEstimado) FROM Pedidos p") // Ajusta 'totalEstimado' al nombre exacto de tu variable en el Model
    BigDecimal sumarTotalPedidos();
}
