package com.example.demo.controller;

import com.example.demo.model.Cliente;
import com.example.demo.dto.LoginRequest;
import com.example.demo.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ClienteController {
    
    @Autowired 
    private ClienteRepository clienteRepository;
    @PostMapping ("/registro")
    public ResponseEntity<?> registrarCliente(@RequestBody Cliente cliente) {
        if (clienteRepository.existsByCorreoElectronico(cliente.getCorreoElectronico())) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "El correo electrónico ya está registrado");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        cliente.setContrasenaHash(cliente.getContrasenaHash());
        Cliente nuevoCliente = clienteRepository.save(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoCliente);
    }

    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(@RequestBody LoginRequest login) {
        Optional<Cliente> clienteOpt = clienteRepository.findByCorreoElectronico(login.getCorreoElectronico());

        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            if (cliente.getContrasenaHash().equals(login.getContrasena())) {
                Map<String, Object> response = new HashMap<>();
                response.put("mensaje", "Login exitoso");
                response.put("cliente", cliente);
                return ResponseEntity.ok(response);
            }
        }

        Map<String, String> error = new HashMap<>();
        error.put("mensaje", "Credenciales incorrectas.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
}
