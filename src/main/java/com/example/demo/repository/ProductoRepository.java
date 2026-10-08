package com.example.demo.repository;

import com.example.demo.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Contar productos con stockDisponible <= 5
    long countByStockDisponibleLessThanEqual(int stockLimite);

    // Obtener los 5 productos con menor stock
    List<Producto> findTop5ByStockDisponibleLessThanEqualOrderByStockDisponibleAsc(int stockLimite);

    // USAR precioBase EN LUGAR DE precio
    @Query("SELECT SUM(p.precioBase * p.stockDisponible) FROM Producto p")
    BigDecimal calcularValorTotalInventario();
}