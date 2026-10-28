//cody by wilson

package com.example.demo.service;

import com.example.demo.dto.CrearCotizacionRequest;
import com.example.demo.dto.DetalleCotizacionRequest;
import com.example.demo.dto.PedidosRequest;
import com.example.demo.model.Cliente;
import com.example.demo.model.Cotizacion;
import com.example.demo.model.Envio;
import com.example.demo.model.EstadoPedido;
import com.example.demo.model.Producto;
import com.example.demo.repository.ClienteRepository;
import com.example.demo.repository.EnvioRepository;
import com.example.demo.repository.PedidoRepository;
import com.example.demo.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PedidosService {

    private final PedidoRepository pedidosRepository;
    private final ClienteRepository clienteRepository;
    private final EnvioRepository envioRepository;
    private final ProductoRepository productoRepository;

    public PedidosService(PedidoRepository pedidosRepository,
                          ClienteRepository clienteRepository,
                          EnvioRepository envioRepository,
                          ProductoRepository productoRepository) {
        this.pedidosRepository = pedidosRepository;
        this.clienteRepository = clienteRepository;
        this.envioRepository = envioRepository;
        this.productoRepository = productoRepository;
    }

    // Todos los pedidos (para el panel de administración)
    public List<PedidosRequest> obtenerTodosLosPedidos() {
        return pedidosRepository.findAll().stream()
            .map(this::aDto)
            .collect(Collectors.toList());
    }

    // Solo los pedidos del cliente indicado (para la página "Mi cuenta")
    public List<PedidosRequest> obtenerPedidosPorCliente(Long clienteId) {
        return pedidosRepository.findByCliente_IdOrderByFechaSolicitudDesc(clienteId).stream()
            .map(this::aDto)
            .collect(Collectors.toList());
    }

    // Crea un pedido nuevo en COTIZACIONES con estado PENDIENTE.
    // El total se calcula AQUÍ con los precios de la base de datos (no se confía en el precio que manda el navegador).
    @Transactional
    public PedidosRequest crearPedido(CrearCotizacionRequest req) {
        if (req.getClienteId() == null) {
            throw new IllegalArgumentException("Debes iniciar sesión para hacer un pedido.");
        }
        if (req.getProductos() == null || req.getProductos().isEmpty()) {
            throw new IllegalArgumentException("Tu carrito está vacío.");
        }

        Cliente cliente = clienteRepository.findById(req.getClienteId())
            .orElseThrow(() -> new IllegalArgumentException("No encontramos tu cuenta. Vuelve a iniciar sesión."));

        Envio envio = buscarEnvio(req);

        BigDecimal total = BigDecimal.ZERO;
        for (DetalleCotizacionRequest item : req.getProductos()) {
            if (item.getProductoId() == null || item.getCantidad() == null || item.getCantidad() <= 0) {
                throw new IllegalArgumentException("Hay un producto o una cantidad no válida en el carrito.");
            }
            Producto producto = productoRepository.findById(item.getProductoId())
                .orElseThrow(() -> new IllegalArgumentException("Un producto de tu carrito ya no existe."));

            Integer stock = producto.getStockDisponible();
            if (stock != null && stock < item.getCantidad()) {
                throw new IllegalArgumentException("No hay suficiente stock de " + producto.getNombre()
                    + " (disponibles: " + stock + ").");
            }
            total = total.add(producto.getPrecioBase().multiply(BigDecimal.valueOf(item.getCantidad())));
        }


         // El total guardado incluye el costo de envío de la zona elegida
        if (envio.getCostoEnvio() != null) {
            total = total.add(envio.getCostoEnvio());
        }
        
        Cotizacion pedido = new Cotizacion();
        pedido.setCliente(cliente);
        pedido.setEnvio(envio);
        pedido.setTotalEstimado(total);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setMetodoPago(
            req.getMetodoPago() == null || req.getMetodoPago().isBlank() ? "CONTRA_ENTREGA" : req.getMetodoPago().trim()
        );

        return aDto(pedidosRepository.save(pedido));
    }

       // update de la funcion
    @Transactional
    public PedidosRequest actualizarEstadoPedido(Long id, EstadoPedido nuevoEstado) {
        Cotizacion pedido = pedidosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));

        pedido.setEstado(nuevoEstado);
        return aDto(pedidosRepository.save(pedido));
    }

    // Busca la zona de envío: por id, o por departamento + municipio.
    // Si el municipio escrito no coincide con ninguno, usa la primera zona de ese departamento.
    private Envio buscarEnvio(CrearCotizacionRequest req) {
        if (req.getEnvioId() != null) {
            return envioRepository.findById(req.getEnvioId())
                .orElseThrow(() -> new IllegalArgumentException("La zona de envío seleccionada no existe."));
        }

        String depto = normalizar(req.getDepartamento());
        String muni = normalizar(req.getMunicipio());
        if (depto.isEmpty()) {
            throw new IllegalArgumentException("Selecciona un departamento de envío.");
        }

        List<Envio> delDepartamento = envioRepository.findAll().stream()
            .filter(e -> normalizar(e.getDepartamento()).equals(depto))
            .collect(Collectors.toList());

        if (delDepartamento.isEmpty()) {
            throw new IllegalArgumentException("Por ahora no hacemos envíos a " + req.getDepartamento() + ".");
        }

        return delDepartamento.stream()
            .filter(e -> normalizar(e.getMunicipio()).equals(muni))
            .findFirst()
            .orElse(delDepartamento.get(0));
    }

    // "Sacatepéquez " -> "sacatepequez" (sin tildes, sin mayúsculas, sin espacios extra)
    private static String normalizar(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto.trim().toLowerCase(), Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "");
    }

    // Convierte una Cotizacion de la base de datos al formato que recibe el frontend
    private PedidosRequest aDto(Cotizacion pedido) {
        return new PedidosRequest(
            pedido.getIdCotizacion(),
            pedido.getCliente() != null ? pedido.getCliente().getNombreCompleto() : "Cliente Desconocido",
            pedido.getFechaSolicitud() != null ? pedido.getFechaSolicitud().toString() : "N/A",
            pedido.getTotalEstimado(),
            pedido.getEstado() != null ? pedido.getEstado().name() : "PENDIENTE",
            pedido.getMetodoPago()
        );
    }
}

//Originl code
/*package com.example.demo.service;

import com.example.demo.dto.PedidosRequest;
import com.example.demo.model.EstadoPedido;
import com.example.demo.model.Cotizacion;
import com.example.demo.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PedidosService {

    private final PedidoRepository pedidosRepository;

    public PedidosService(PedidoRepository pedidosRepository) {
        this.pedidosRepository = pedidosRepository;
    }

    // Todos los pedidos (para el panel de administración)
    public List<PedidosRequest> obtenerTodosLosPedidos() {
        return pedidosRepository.findAll().stream()
            .map(this::aDto)
            .collect(Collectors.toList());
    }

    // Solo los pedidos del cliente indicado (para la página "Mi cuenta")
    public List<PedidosRequest> obtenerPedidosPorCliente(Long clienteId) {
        return pedidosRepository.findByCliente_IdOrderByFechaSolicitudDesc(clienteId).stream()
            .map(this::aDto)
            .collect(Collectors.toList());
    }

    // Convierte una Cotizacion de la base de datos al formato que recibe el frontend
    private PedidosRequest aDto(Cotizacion pedido) {
        return new PedidosRequest(
            pedido.getIdCotizacion(),
            pedido.getCliente() != null ? pedido.getCliente().getNombreCompleto() : "Cliente Desconocido",
            pedido.getFechaSolicitud() != null ? pedido.getFechaSolicitud().toString() : "N/A",
            pedido.getTotalEstimado(),
            pedido.getEstado() != null ? pedido.getEstado().name() : "PENDIENTE",
            pedido.getMetodoPago()
        );
    }

    public Cotizacion actualizarEstadoPedido(Long id, EstadoPedido nuevoEstado) {
        Cotizacion pedido = pedidosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));

        pedido.setEstado(nuevoEstado);
        return pedidosRepository.save(pedido);
    }
}
*/
