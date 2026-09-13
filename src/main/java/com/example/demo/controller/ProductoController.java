package com.example.demo.controller;

import com.example.demo.dto.VerificarStockRequest;
import com.example.demo.model.Categoria;
import com.example.demo.model.Producto;
import com.example.demo.repository.CategoriaRepository;
import com.example.demo.repository.ProductoRepository;
import com.example.demo.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private S3Service s3Service;

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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<?> crearProducto(
            @RequestParam("nombre") String nombre,
            @RequestParam("precioBase") BigDecimal precioBase,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("stockDisponible") Integer stockDisponible,
            @RequestParam("categoria.id") Long categoriaId,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) {
        try {
            Producto producto = new Producto();
            producto.setNombre(nombre);
            producto.setPrecioBase(precioBase);
            producto.setDescripcion(descripcion);
            producto.setStockDisponible(stockDisponible);

            // 1. Buscar la categoría requerida por la restricción NOT NULL de Oracle
            Categoria categoria = categoriaRepository.findById(categoriaId)
                    .orElseThrow(() -> new RuntimeException("La categoría con ID " + categoriaId + " no existe en la base de datos."));
            producto.setCategoria(categoria);

            // 2. Subir imagen a S3 si se seleccionó archivo
            if (imagen != null && !imagen.isEmpty()) {
                String imageUrl = s3Service.uploadFile(imagen);
                producto.setImagenUrl(imageUrl);
            }

            // 3. Persistir en Oracle
            Producto productoGuardado = productoRepository.save(producto);
            return ResponseEntity.status(HttpStatus.CREATED).body(productoGuardado);

        } catch (Exception e) {
            System.err.println("=== ERROR AL CREAR PRODUCTO ===");
            e.printStackTrace(); // Garantiza que el error salga impreso en Render
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