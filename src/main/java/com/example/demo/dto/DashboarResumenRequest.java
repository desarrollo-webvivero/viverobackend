package com.example.demo.dto;

import java.math.BigDecimal;

public class DashboarResumenRequest {
    private long totalCompras;
    private BigDecimal totalMonto;

    public DashboarResumenRequest() {
        // Default constructor
    }

    public DashboarResumenRequest(long totalCompras, BigDecimal totalMonto) {
        this.totalCompras = totalCompras;
        this.totalMonto = totalMonto;
    }

    public long getTotalCompras() {
        return totalCompras;
    }

    public void setTotalCompras(long totalCompras) {
        this.totalCompras = totalCompras;
    }

    public BigDecimal getTotalMonto() {
        return totalMonto;
    }

    public void setTotalMonto(BigDecimal totalMonto) {
        this.totalMonto = totalMonto;
    }
}