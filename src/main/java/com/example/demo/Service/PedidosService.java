package com.example.demo.service;

import com.example.demo.dto.CrearCotizacionRequest;
import com.example.demo.dto.DetalleCotizacionRequest;
import com.example.demo.dto.PedidoDetalleResponse;
import com.example.demo.dto.PedidosRequest;
import com.example.demo.model.Cliente;
import com.example.demo.model.Cotizacion;
import com.example.demo.model.DetalleCotizacion;
import com.example.demo.model.Envio;
import com.example.demo.model.EstadoPedido;
import com.example.demo.model.Producto;
import com.example.demo.repository.ClienteRepository;
import com.example.demo.repository.DetalleCotizacionRepository;
import com.example.demo.repository.EnvioRepository;
import com.example.demo.repository.PedidoRepository;
import com.example.demo.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PedidosService {

    private final PedidoRepository pedidosRepository;
    private final ClienteRepository clienteRepository;
    private final EnvioRepository envioRepository;
    private final ProductoRepository productoRepository;
    private final DetalleCotizacionRepository detalleRepository;

    public PedidosService(PedidoRepository pedidosRepository,
                          ClienteRepository clienteRepository,
                          EnvioRepository envioRepository,
                          ProductoRepository productoRepository,
                          DetalleCotizacionRepository detalleRepository) {
        this.pedidosRepository = pedidosRepository;
        this.clienteRepository = clienteRepository;
        this.envioRepository = envioRepository;
        this.productoRepository = productoRepository;
        this.detalleRepository = detalleRepository;
    }

    // Largo máximo de COTIZACIONES.DIRECCION_ENTREGA
    private static final int LARGO_MAX_DIRECCION = 255;

    // Un producto del carrito ya validado, con el precio de la base de datos
    public record LineaPedido(Long productoId, String nombre, Integer cantidad, BigDecimal precioUnitario) {}

    // Datos ya validados de un pedido, listos para guardarse
    public record PedidoCalculado(Cliente cliente, Envio envio, BigDecimal total,
                                  List<LineaPedido> lineas, String direccionEntrega) {}

    // Todos los pedidos
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

    // Detalle completo de un pedido: productos, dirección, envío y totales.
    // Si se manda clienteId, solo lo devuelve si el pedido es de ese cliente.
    @Transactional(readOnly = true)
    public PedidoDetalleResponse obtenerDetalle(Long pedidoId, Long clienteId) {
        Cotizacion pedido = pedidosRepository.findById(pedidoId)
            .filter(p -> clienteId == null
                || (p.getCliente() != null && clienteId.equals(p.getCliente().getId())))
            .orElseThrow(() -> new IllegalArgumentException("No encontramos ese pedido."));

        List<DetalleCotizacion> detalles = detalleRepository.findByCotizacion_IdCotizacionOrderByIdAsc(pedidoId);

        // Nombres de los productos
        List<Long> ids = detalles.stream().map(DetalleCotizacion::getProductoId)
            .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, String> nombres = productoRepository.findAllById(ids).stream()
            .collect(Collectors.toMap(Producto::getId, Producto::getNombre, (a, b) -> a));
        Function<Long, String> nombreDe = id ->
            nombres.getOrDefault(id, "Producto #" + id + " (ya no está en el catálogo)");

        List<PedidoDetalleResponse.Producto> productos = detalles.stream()
            .map(d -> new PedidoDetalleResponse.Producto(
                nombreDe.apply(d.getProductoId()),
                d.getCantidad(),
                d.getPrecioUnitario(),
                d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad()))
            ))
            .collect(Collectors.toList());

        BigDecimal total = pedido.getTotalEstimado() != null ? pedido.getTotalEstimado() : BigDecimal.ZERO;
        Envio envio = pedido.getEnvio();

        // Si tenemos los productos, el envío es lo que falta para llegar al total.
        // En pedidos antiguos (sin productos guardados) usamos el costo actual de la zona.
        BigDecimal subtotal;
        BigDecimal costoEnvio;
        if (!productos.isEmpty()) {
            subtotal = productos.stream().map(PedidoDetalleResponse.Producto::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            costoEnvio = total.subtract(subtotal);
        } else {
            costoEnvio = envio != null && envio.getCostoEnvio() != null ? envio.getCostoEnvio() : BigDecimal.ZERO;
            subtotal = total.subtract(costoEnvio);
        }

        PedidoDetalleResponse r = new PedidoDetalleResponse();
        r.setId(pedido.getIdCotizacion());
        r.setFecha(pedido.getFechaSolicitud() != null ? pedido.getFechaSolicitud().toString() : null);
        r.setEstado(pedido.getEstado() != null ? pedido.getEstado().name() : "PENDIENTE");
        r.setMetodo(pedido.getMetodoPago());
        r.setSubtotal(subtotal);
        r.setCostoEnvio(costoEnvio);
        r.setTotal(total);
        if (envio != null) {
            r.setDepartamento(envio.getDepartamento());
            r.setMunicipio(envio.getMunicipio());
            r.setZonaReferencia(envio.getZonaReferencia());
        }
        r.setDireccionEntrega(pedido.getDireccionEntrega());
        r.setProductos(productos);
        if (pedido.getCliente() != null) {
            r.setClienteNombre(pedido.getCliente().getNombreCompleto());
            r.setClienteTelefono(pedido.getCliente().getTelefono());
            r.setClienteCorreo(pedido.getCliente().getCorreoElectronico());
        }
        return r;
    }

    // Valida el pedido y calcula el total (productos + envío) con los precios de la base de datos.
    // No guarda nada.
    @Transactional(readOnly = true)
    public PedidoCalculado calcularPedido(CrearCotizacionRequest req) {
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
        List<LineaPedido> lineas = new ArrayList<>();
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
            lineas.add(new LineaPedido(producto.getId(), producto.getNombre(), item.getCantidad(), producto.getPrecioBase()));
        }

        // El total incluye el costo de envío de la zona elegida
        if (envio.getCostoEnvio() != null) {
            total = total.add(envio.getCostoEnvio());
        }

        String direccion = req.getDireccionEntrega() == null ? null : req.getDireccionEntrega().trim();
        if (direccion != null && direccion.length() > LARGO_MAX_DIRECCION) {
            direccion = direccion.substring(0, LARGO_MAX_DIRECCION);
        }

        return new PedidoCalculado(cliente, envio, total, lineas, direccion);
    }

    // Guarda el pedido (COTIZACIONES) y sus productos (DETALLE_COTIZACIONES) con estado PENDIENTE
    @Transactional
    public PedidosRequest guardarPedido(PedidoCalculado calculado, String metodoPago) {
        Cotizacion pedido = new Cotizacion();
        pedido.setCliente(calculado.cliente());
        pedido.setEnvio(calculado.envio());
        pedido.setTotalEstimado(calculado.total());
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setMetodoPago(metodoPago == null || metodoPago.isBlank() ? "CONTRA_ENTREGA" : metodoPago.trim());
        pedido.setDireccionEntrega(calculado.direccionEntrega());
        Cotizacion guardado = pedidosRepository.save(pedido);

        for (LineaPedido linea : calculado.lineas()) {
            DetalleCotizacion detalle = new DetalleCotizacion();
            detalle.setCotizacion(guardado);
            detalle.setProductoId(linea.productoId());
            detalle.setCantidad(linea.cantidad());
            detalle.setPrecioUnitario(linea.precioUnitario());
            detalleRepository.save(detalle);
        }

        return aDto(guardado);
    }

    // Pedido "contra entrega": calcular y guardar en un solo paso
    @Transactional
    public PedidosRequest crearPedido(CrearCotizacionRequest req) {
        return guardarPedido(calcularPedido(req), req.getMetodoPago());
    }

    // Devuelve el DTO (no la entidad): así no se envían datos del cliente como su contraseña cifrada
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
