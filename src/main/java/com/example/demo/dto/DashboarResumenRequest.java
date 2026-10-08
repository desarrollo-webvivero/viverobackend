package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboarResumenRequest {

    // Métricas principales (Tarjetas / KPIs)
    private BigDecimal ventasDelMes;
    private long ordenesDelMes;
    private BigDecimal valorInventario;
    private long totalPlantasActivas;
    private long alertasStockBajo;

    // Listas para las tablas del Dashboard
    private List<PlantaResumenDTO> plantasEnPeligro;
    private List<PedidoResumenDTO> ultimosPedidos;

    // Constructor vacío (requerido por frameworks)
    public DashboarResumenRequest() {
    }

    // Constructor completo
    public DashboarResumenRequest(BigDecimal ventasDelMes, long ordenesDelMes, BigDecimal valorInventario,
                                 long totalPlantasActivas, long alertasStockBajo,
                                 List<PlantaResumenDTO> plantasEnPeligro, List<PedidoResumenDTO> ultimosPedidos) {
        this.ventasDelMes = ventasDelMes;
        this.ordenesDelMes = ordenesDelMes;
        this.valorInventario = valorInventario;
        this.totalPlantasActivas = totalPlantasActivas;
        this.alertasStockBajo = alertasStockBajo;
        this.plantasEnPeligro = plantasEnPeligro;
        this.ultimosPedidos = ultimosPedidos;
    }

    // --- GETTERS Y SETTERS ---

    public BigDecimal getVentasDelMes() {
        return ventasDelMes;
    }

    public void setVentasDelMes(BigDecimal ventasDelMes) {
        this.ventasDelMes = ventasDelMes;
    }

    public long getOrdenesDelMes() {
        return ordenesDelMes;
    }

    public void setOrdenesDelMes(long ordenesDelMes) {
        this.ordenesDelMes = ordenesDelMes;
    }

    public BigDecimal getValorInventario() {
        return valorInventario;
    }

    public void setValorInventario(BigDecimal valorInventario) {
        this.valorInventario = valorInventario;
    }

    public long getTotalPlantasActivas() {
        return totalPlantasActivas;
    }

    public void setTotalPlantasActivas(long totalPlantasActivas) {
        this.totalPlantasActivas = totalPlantasActivas;
    }

    public long getAlertasStockBajo() {
        return alertasStockBajo;
    }

    public void setAlertasStockBajo(long alertasStockBajo) {
        this.alertasStockBajo = alertasStockBajo;
    }

    public List<PlantaResumenDTO> getPlantasEnPeligro() {
        return plantasEnPeligro;
    }

    public void setPlantasEnPeligro(List<PlantaResumenDTO> plantasEnPeligro) {
        this.plantasEnPeligro = plantasEnPeligro;
    }

    public List<PedidoResumenDTO> getUltimosPedidos() {
        return ultimosPedidos;
    }

    public void setUltimosPedidos(List<PedidoResumenDTO> ultimosPedidos) {
        this.ultimosPedidos = ultimosPedidos;
    }

    // --- CLASES DTO INTERNAS PARA TABLAS ---

    public static class PlantaResumenDTO {
        private Long id;
        private String nombre;
        private String imagenUrl;
        private int stockDisponible;

        public PlantaResumenDTO() {}

        public PlantaResumenDTO(Long id, String nombre, String imagenUrl, int stockDisponible) {
            this.id = id;
            this.nombre = nombre;
            this.imagenUrl = imagenUrl;
            this.stockDisponible = stockDisponible;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getImagenUrl() { return imagenUrl; }
        public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

        public int getStockDisponible() { return stockDisponible; }
        public void setStockDisponible(int stockDisponible) { this.stockDisponible = stockDisponible; }
    }

    public static class PedidoResumenDTO {
        private Long id;
        private String cliente;
        private String fecha;
        private BigDecimal total;
        private String estado;

        public PedidoResumenDTO() {}

        public PedidoResumenDTO(Long id, String cliente, String fecha, BigDecimal total, String estado) {
            this.id = id;
            this.cliente = cliente;
            this.fecha = fecha;
            this.total = total;
            this.estado = estado;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getCliente() { return cliente; }
        public void setCliente(String cliente) { this.cliente = cliente; }

        public String getFecha() { return fecha; }
        public void setFecha(String fecha) { this.fecha = fecha; }

        public BigDecimal getTotal() { return total; }
        public void setTotal(BigDecimal total) { this.total = total; }

        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }
    }
}