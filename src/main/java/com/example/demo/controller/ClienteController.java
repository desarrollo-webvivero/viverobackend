package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegistroClienteRequest;
import com.example.demo.model.Cliente;
import com.example.demo.repository.ClienteRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
public class ClienteController {

    @Autowired
    private ClienteRepository clienteRepository;

    @PostMapping("/registro")
    public ResponseEntity<?> registrarCliente(
            @RequestBody RegistroClienteRequest request) {

        if (request.getNombreCompleto() == null ||
            request.getNombreCompleto().trim().isEmpty()) {

            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "El nombre es obligatorio."));
        }

        if (request.getCorreoElectronico() == null ||
            request.getCorreoElectronico().trim().isEmpty()) {

            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "El correo es obligatorio."));
        }

        if (request.getContrasena() == null ||
            request.getContrasena().trim().isEmpty()) {

            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "La contraseña es obligatoria."));
        }

        if (clienteRepository.existsByCorreoElectronico(
                request.getCorreoElectronico())) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "mensaje",
                            "El correo electrónico ya está registrado."
                    ));
        }

        Cliente cliente = new Cliente();

        cliente.setNombreCompleto(request.getNombreCompleto());
        cliente.setTelefono(request.getTelefono());
        cliente.setCorreoElectronico(request.getCorreoElectronico());

        // Temporalmente:
        cliente.setContrasenaHash(request.getContrasena());

        cliente.setCuentaVerificada(0);

        Cliente nuevoCliente = clienteRepository.save(cliente);
        Map<String, Object> respuesta = new HashMap<>();

           respuesta.put("mensaje", "Cliente registrado correctamente");
           respuesta.put("clienteId", nuevoCliente.getId());

            return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(respuesta);

    
    }


    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(
            @RequestBody LoginRequest login) {

        Optional<Cliente> clienteOpt =
                clienteRepository.findByCorreoElectronico(
                        login.getCorreoElectronico());

        if (clienteOpt.isPresent()) {

            Cliente cliente = clienteOpt.get();

            if (cliente.getContrasenaHash()
                    .equals(login.getContrasena())) {

                Map<String, Object> response = new HashMap<>();

                 response.put("mensaje", "Login exitoso");
                 response.put("clienteId", cliente.getId());
                 response.put("nombreCompleto", cliente.getNombreCompleto());
                 response.put("correoElectronico",
                 cliente.getCorreoElectronico());

                return ResponseEntity.ok(response);
            }
        }

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "mensaje",
                        "Credenciales incorrectas."
                ));
    }
}