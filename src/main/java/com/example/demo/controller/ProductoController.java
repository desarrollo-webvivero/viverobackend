package com.example.demo.controller;

import com.example.demo.dto.VerificarStockRequest;
import com.example.demo.model.Categoria;
import com.example.demo.model.Producto;
import com.example.demo.repository.CategoriaRepository;
import com.example.demo.repository.ProductoRepository;
import com.example.demo.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*") // Habilita peticiones desde AWS Amplify
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private S3Service s3Service;

    // Nuevo endpoint para subir la imagen a S3
    @PostMapping("/upload")
    public ResponseEntity<?> subirImagen(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = s3Service.uploadFile(file);
            Map<String, String> response = new HashMap<>();
            response.put("url", imageUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error al subir la imagen a S3: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping
    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    @PostMapping
    @Transactional // Garantiza el COMMIT automático en la base de datos Oracle
    public ResponseEntity<?> crearProducto(@RequestBody Producto producto) {
        try {
            if (producto.getCategoria() != null && producto.getCategoria().getId() != null) {
                Categoria categoriaCompleta = categoriaRepository.findById(producto.getCategoria().getId())
                        .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + producto.getCategoria().getId()));
                producto.setCategoria(categoriaCompleta);
            }

            Producto productoGuardado = productoRepository.save(producto);
            return ResponseEntity.status(HttpStatus.CREATED).body(productoGuardado);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error al guardar el producto: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
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