package com.example.demo.controller;

import com.example.demo.service.VerificacionService;
import com.example.demo.service.VerificacionService.Resultado;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegistroClienteRequest;
import com.example.demo.dto.ReenviarCodigoRequest;
import com.example.demo.dto.VerificarCuentaRequest;
import com.example.demo.model.CanalVerificacion;
import com.example.demo.model.Cliente;
import com.example.demo.repository.ClienteRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
public class ClienteController {

    private static final Pattern CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern TELEFONO_E164 = Pattern.compile("^\\+[1-9]\\d{7,14}$");

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificacionService verificacionService;

    // Inyección por constructor: Spring nos entrega lo que necesitamos al crear el controller.
    public ClienteController(ClienteRepository clienteRepository,
                             PasswordEncoder passwordEncoder,
                             VerificacionService verificacionService) {
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificacionService = verificacionService;
    }

    // ===================================================================
    // 1) REGISTRO: crea la cuenta (contraseña hasheada) y envía el código
    // ===================================================================
    @PostMapping("/registro")
    public ResponseEntity<?> registrarCliente(@RequestBody RegistroClienteRequest request) {

        String nombre = limpiar(request.getNombreCompleto());
        String correo = limpiar(request.getCorreoElectronico());
        String contrasena = request.getContrasena() == null ? "" : request.getContrasena();
        String telefonoCrudo = limpiar(request.getTelefono());
        CanalVerificacion canal = CanalVerificacion.desde(request.getCanalVerificacion());

        if (nombre.isEmpty()) {
            return error(HttpStatus.BAD_REQUEST, "El nombre es obligatorio.");
        }
        if (!CORREO.matcher(correo).matches()) {
            return error(HttpStatus.BAD_REQUEST, "El correo electrónico no es válido.");
        }
        if (!contrasenaSegura(contrasena)) {
            return error(HttpStatus.BAD_REQUEST,
                    "La contraseña debe tener al menos 8 caracteres, con letras y números.");
        }
        if (canal == null) {
            return error(HttpStatus.BAD_REQUEST,
                    "Elige cómo recibir el código: EMAIL o WHATSAPP.");
        }

        String telefono = null;
        if (!telefonoCrudo.isEmpty()) {
            telefono = normalizarTelefono(telefonoCrudo);
            if (telefono == null) {
                return error(HttpStatus.BAD_REQUEST,
                        "El teléfono no es válido. Ejemplo: 55551234 o +50255551234.");
            }
        }
        if (canal == CanalVerificacion.WHATSAPP && telefono == null) {
            return error(HttpStatus.BAD_REQUEST,
                    "Para recibir el código por WhatsApp necesitas indicar tu teléfono.");
        }

        if (clienteRepository.existsByCorreoElectronico(correo)) {
            return error(HttpStatus.CONFLICT, "El correo electrónico ya está registrado.");
        }

        Cliente cliente = new Cliente();
        cliente.setNombreCompleto(nombre);
        cliente.setTelefono(telefono);
        cliente.setCorreoElectronico(correo);
        // ¡Ya NO guardamos la contraseña: guardamos su hash BCrypt!
        cliente.setContrasenaHash(passwordEncoder.encode(contrasena));
        cliente.setCuentaVerificada(0);

        Cliente nuevo = clienteRepository.save(cliente);

        boolean codigoEnviado = true;
        try {
            verificacionService.generarYEnviarCodigo(nuevo, canal);
        } catch (Exception e) {
            codigoEnviado = false;
            System.err.println("No se pudo enviar el código: " + e.getMessage());
        }

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", codigoEnviado
                ? "Cuenta creada. Te enviamos un código de verificación por " + canal + "."
                : "Cuenta creada, pero no pudimos enviar el código. Usa 'reenviar-codigo'.");
        respuesta.put("clienteId", nuevo.getId());
        respuesta.put("canalVerificacion", canal);
        respuesta.put("codigoEnviado", codigoEnviado);

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // ===================================================================
    // 2) VERIFICAR: el usuario escribe el código de 6 dígitos
    // ===================================================================
    @PostMapping("/verificar")
    public ResponseEntity<?> verificarCuenta(@RequestBody VerificarCuentaRequest request) {

        Optional<Cliente> clienteOpt = buscarPorCorreo(request.correoElectronico());
        if (clienteOpt.isEmpty()) {
            return error(HttpStatus.BAD_REQUEST, "Código incorrecto o vencido.");
        }

        Resultado resultado = verificacionService.verificarCodigo(clienteOpt.get(), request.codigo());

        return switch (resultado) {
            case OK -> ResponseEntity.ok(Map.of("mensaje", "Cuenta verificada. Ya puedes iniciar sesión."));
            case YA_VERIFICADA -> ResponseEntity.ok(Map.of("mensaje", "Tu cuenta ya estaba verificada."));
            case CODIGO_INCORRECTO -> error(HttpStatus.BAD_REQUEST, "Código incorrecto.");
            case CODIGO_EXPIRADO -> error(HttpStatus.BAD_REQUEST,
                    "El código venció. Solicita uno nuevo.");
            case DEMASIADOS_INTENTOS -> error(HttpStatus.TOO_MANY_REQUESTS,
                    "Demasiados intentos. Solicita un código nuevo.");
        };
    }

    // ===================================================================
    // 3) REENVIAR CÓDIGO (por el mismo canal u otro)
    // ===================================================================
    @PostMapping("/reenviar-codigo")
    public ResponseEntity<?> reenviarCodigo(@RequestBody ReenviarCodigoRequest request) {

        CanalVerificacion canal = CanalVerificacion.desde(request.canalVerificacion());
        if (canal == null) {
            return error(HttpStatus.BAD_REQUEST, "Elige un canal: EMAIL o WHATSAPP.");
        }

        // Respuesta genérica a propósito: no revelamos si el correo existe o no.
        Map<String, String> generica = Map.of(
                "mensaje", "Si la cuenta existe y no está verificada, enviaremos un nuevo código.");

        Optional<Cliente> clienteOpt = buscarPorCorreo(request.correoElectronico());
        if (clienteOpt.isEmpty() || clienteOpt.get().getCuentaVerificada() == 1) {
            return ResponseEntity.ok(generica);
        }

        try {
            verificacionService.generarYEnviarCodigo(clienteOpt.get(), canal);
        } catch (Exception e) {
            System.err.println("No se pudo reenviar el código: " + e.getMessage());
            return error(HttpStatus.BAD_GATEWAY,
                    "No pudimos enviar el código. Intenta con otro canal o más tarde.");
        }
        return ResponseEntity.ok(generica);
    }

    // ===================================================================
    // 4) LOGIN: solo entra quien tiene la cuenta verificada
    // ===================================================================
    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(@RequestBody LoginRequest login) {

        Optional<Cliente> clienteOpt = buscarPorCorreo(login.getCorreoElectronico());

        // matches(): BCrypt compara la contraseña escrita contra el hash guardado.
        boolean credencialesOk = clienteOpt.isPresent()
                && login.getContrasena() != null
                && passwordEncoder.matches(login.getContrasena(), clienteOpt.get().getContrasenaHash());

        if (!credencialesOk) {
            return error(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas.");
        }

        Cliente cliente = clienteOpt.get();

        if (cliente.getCuentaVerificada() == null || cliente.getCuentaVerificada() != 1) {
            Map<String, Object> noVerificada = new HashMap<>();
            noVerificada.put("mensaje", "Debes verificar tu cuenta antes de iniciar sesión.");
            noVerificada.put("requiereVerificacion", true);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(noVerificada);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Login exitoso");
        response.put("clienteId", cliente.getId());
        response.put("nombreCompleto", cliente.getNombreCompleto());
        response.put("correoElectronico", cliente.getCorreoElectronico());
        response.put("telefono", cliente.getTelefono());
        return ResponseEntity.ok(response);
    }

    // ===================== funciones auxiliares =====================

    private Optional<Cliente> buscarPorCorreo(String correo) {
        String limpio = limpiar(correo);
        if (limpio.isEmpty()) return Optional.empty();
        return clienteRepository.findByCorreoElectronico(limpio);
    }

    private String limpiar(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private boolean contrasenaSegura(String c) {
        return c.length() >= 8
                && c.chars().anyMatch(Character::isLetter)
                && c.chars().anyMatch(Character::isDigit);
    }

    /** "5555-1234" -> "+50255551234". Devuelve null si el teléfono no es válido. */
    private String normalizarTelefono(String crudo) {
        String t = crudo.replaceAll("[\\s\\-()]", "");
        if (t.matches("^\\d{8}$")) {
            t = "+502" + t; // números locales de Guatemala
        }
        return TELEFONO_E164.matcher(t).matches() ? t : null;
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("mensaje", mensaje));
    }
}
