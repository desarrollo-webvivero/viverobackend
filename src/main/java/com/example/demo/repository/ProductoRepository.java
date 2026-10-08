package com.example.demo.repository;

import com.example.demo.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Contar productos con stock <= 5
    long countByStockDisponibleLessThanEqual(int stockLimite);

    // Obtener los 5 productos con menor stock (para la tabla del dashboard)
    List<Producto> findTop5ByStockDisponibleLessThanEqualOrderByStockDisponibleAsc(int stockLimite);

    // Sumar el valor total del inventario (precio * stockDisponible)
    @Query("SELECT SUM(p.precio * p.stockDisponible) FROM Producto p")
    BigDecimal calcularValorTotalInventario();
}