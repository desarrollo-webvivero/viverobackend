package com.example.demo.controller;

import com.example.demo.dto.VerificarStockRequest;
import com.example.demo.model.Categoria;
import com.example.demo.model.Producto;
import com.example.demo.repository.CategoriaRepository;
import com.example.demo.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;


@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @GetMapping
    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Producto> crearProducto(@RequestBody Producto producto) {
     if (producto.getCategoria() != null && producto.getCategoria().getId() != null) {
        Categoria categoriaCompleta = categoriaRepository.findById(producto.getCategoria().getId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        producto.setCategoria(categoriaCompleta);
    }
    
    Producto productoGuardado = productoRepository.save(producto);
    return ResponseEntity.ok(productoGuardado);
   }

   @PostMapping("/verificar-stock")
public ResponseEntity<?> verificarStock(@RequestBody VerificarStockRequest request) {
    Optional<Producto> productoOpt = productoRepository.findById(request.getProductoId());

    if (productoOpt.isEmpty()) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", "Producto no encontrado.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    Producto producto = productoOpt.get();

    if (producto.getStockDisponible() < request.getCantidad()) {
        Map<String, Object> response = new HashMap<>();
        response.put("disponible", false);
        response.put("mensaje", "Cantidad insuficiente. Solo quedan " + producto.getStockDisponible() + " unidades.");
        response.put("stockActual", producto.getStockDisponible());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    Map<String, Object> response = new HashMap<>();
    response.put("disponible", true);
    response.put("mensaje", "Stock disponible.");
    return ResponseEntity.ok(response);
}
}