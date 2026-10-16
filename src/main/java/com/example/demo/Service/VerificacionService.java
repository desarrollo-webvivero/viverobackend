package com.example.demo.service;

import com.example.demo.service.notificacion.Notificador;
import com.example.demo.model.CanalVerificacion;
import com.example.demo.model.Cliente;
import com.example.demo.repository.ClienteRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Se encarga de TODO lo relacionado con el código de verificación:
 * crearlo, enviarlo y comprobarlo.
 */
@Service
public class VerificacionService {

    public enum Resultado { OK, YA_VERIFICADA, CODIGO_INCORRECTO, CODIGO_EXPIRADO, DEMASIADOS_INTENTOS }

    private static final int MINUTOS_VIGENCIA = 10;
    private static final int MAX_INTENTOS = 5;

    // SecureRandom es aleatorio "de verdad" (apto para seguridad). Random normal es predecible.
    private final SecureRandom random = new SecureRandom();

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final Map<CanalVerificacion, Notificador> notificadores = new EnumMap<>(CanalVerificacion.class);

    // Spring nos pasa AUTOMÁTICAMENTE todos los Notificador que existan (email y whatsapp).
    public VerificacionService(ClienteRepository clienteRepository,
                               PasswordEncoder passwordEncoder,
                               List<Notificador> listaNotificadores) {
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
        for (Notificador n : listaNotificadores) {
            notificadores.put(n.canal(), n);
        }
    }

    /** Genera un código nuevo, lo guarda (hasheado) y lo envía por el canal elegido. */
    public void generarYEnviarCodigo(Cliente cliente, CanalVerificacion canal) {
        String codigo = String.format("%06d", random.nextInt(1_000_000)); // "004821", "730155", ...

        cliente.setTokenVerificacion(passwordEncoder.encode(codigo));
        cliente.setTokenExpira(LocalDateTime.now().plusMinutes(MINUTOS_VIGENCIA));
        cliente.setTokenIntentos(0);
        clienteRepository.save(cliente);

        notificadores.get(canal).enviarCodigo(cliente, codigo);
    }

    /** Comprueba el código que escribió el usuario. */
    public Resultado verificarCodigo(Cliente cliente, String codigoIngresado) {

        if (cliente.getCuentaVerificada() != null && cliente.getCuentaVerificada() == 1) {
            return Resultado.YA_VERIFICADA;
        }

        if (cliente.getTokenVerificacion() == null
                || cliente.getTokenExpira() == null
                || LocalDateTime.now().isAfter(cliente.getTokenExpira())) {
            return Resultado.CODIGO_EXPIRADO;
        }

        if (cliente.getTokenIntentos() >= MAX_INTENTOS) {
            return Resultado.DEMASIADOS_INTENTOS;
        }

        if (codigoIngresado == null
                || !passwordEncoder.matches(codigoIngresado.trim(), cliente.getTokenVerificacion())) {
            cliente.setTokenIntentos(cliente.getTokenIntentos() + 1);
            clienteRepository.save(cliente);
            return Resultado.CODIGO_INCORRECTO;
        }

        // Todo bien: verificamos la cuenta y borramos el código (ya no sirve).
        cliente.setCuentaVerificada(1);
        cliente.setTokenVerificacion(null);
        cliente.setTokenExpira(null);
        cliente.setTokenIntentos(0);
        clienteRepository.save(cliente);
        return Resultado.OK;
    }
}
