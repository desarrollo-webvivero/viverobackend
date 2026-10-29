package com.example.demo.repository;

import com.example.demo.model.DetalleCotizacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleCotizacionRepository extends JpaRepository<DetalleCotizacion, Long> {

    // Productos de un pedido
    List<DetalleCotizacion> findByCotizacion_IdCotizacionOrderByIdAsc(Long idCotizacion);
}
