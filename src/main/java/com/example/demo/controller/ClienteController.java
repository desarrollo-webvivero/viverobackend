package com.example.demo.controller;

import com.example.demo.model.Cliente;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.VerificacionRequest;
import com.example.demo.repository.ClienteRepository;
import com.example.demo.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Permite peticiones desde React
public class ClienteController {

    @Autowired 
    private ClienteRepository clienteRepository;

    @Autowired
    private EmailService emailService;

    // 1. REGISTRO (Genera el PIN de 6 dígitos y envía correo)
    @PostMapping("/registro")
    public ResponseEntity<?> registrarCliente(@RequestBody Cliente cliente) {
        if (clienteRepository.existsByCorreoElectronico(cliente.getCorreoElectronico())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "El correo electrónico ya está registrado");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        // Generar PIN aleatorio de 6 dígitos
        String codigoPIN = String.format("%06d", new Random().nextInt(999999));
        
        cliente.setCuentaVerificada(0); // No verificado aún
        cliente.setTokenVerificacion(codigoPIN);
        cliente.setFechaExpiracionToken(LocalDateTime.now().plusMinutes(15)); // Expira en 15 mins

        Cliente nuevoCliente = clienteRepository.save(cliente);

        // Enviar correo electrónico
        try {
            emailService.enviarCodigoVerificacion(cliente.getCorreoElectronico(), codigoPIN);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error al enviar el correo de verificación: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Usuario registrado. Revisa tu correo para ingresar el código.");
        response.put("email", nuevoCliente.getCorreoElectronico());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. VERIFICACIÓN DEL CÓDIGO DE 6 DÍGITOS
    @PostMapping("/verify-email")
    public ResponseEntity<?> verificarCodigo(@RequestBody VerificacionRequest req) {
        Optional<Cliente> clienteOpt = clienteRepository.findByCorreoElectronico(req.getEmail());

        if (clienteOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Usuario no encontrado.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        Cliente cliente = clienteOpt.get();

        // Validar si el código coincide
        if (cliente.getTokenVerificacion() == null || !cliente.getTokenVerificacion().equals(req.getCodigo())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Código de verificación incorrecto.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        // Validar si el código ya expiró
        if (cliente.getFechaExpiracionToken().isBefore(LocalDateTime.now())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "El código ha expirado. Solicita uno nuevo.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        // Activar la cuenta
        cliente.setCuentaVerificada(1);
        cliente.setTokenVerificacion(null); // Limpiar el token usado
        cliente.setFechaExpiracionToken(null);
        clienteRepository.save(cliente);

        // Respuesta compatible con la estructura que espera tu AuthContext de React
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", cliente.getId());
        userMap.put("nombre", cliente.getNombreCompleto());
        userMap.put("email", cliente.getCorreoElectronico());
        userMap.put("is_verified", true);

        Map<String, Object> response = new HashMap<>();
        response.put("token", "JWT_MAQUETA_O_TOKEN_REAL"); // Si usas Spring Security / JWT
        response.put("user", userMap);

        return ResponseEntity.ok(response);
    }

    // 3. INICIO DE SESIÓN
    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(@RequestBody LoginRequest login) {
        Optional<Cliente> clienteOpt = clienteRepository.findByCorreoElectronico(login.getCorreoElectronico());

        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            
            // Validar contraseña
            if (cliente.getContrasenaHash().equals(login.getContrasena())) {
                
                // Verificar si la cuenta ya confirmó el correo
                if (cliente.getCuentaVerificada() == 0) {
                    Map<String, String> error = new HashMap<>();
                    error.put("message", "Debes verificar tu correo electrónico antes de ingresar.");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                }

                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", cliente.getId());
                userMap.put("nombre", cliente.getNombreCompleto());
                userMap.put("email", cliente.getCorreoElectronico());
                userMap.put("is_verified", true);

                Map<String, Object> response = new HashMap<>();
                response.put("token", "JWT_MAQUETA_O_TOKEN_REAL");
                response.put("user", userMap);
                return ResponseEntity.ok(response);
            }
        }

        Map<String, String> error = new HashMap<>();
        error.put("message", "Credenciales incorrectas.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
}