package com.example.demo.controller;

import com.example.demo.dto.DashboarResumenRequest;
import com.example.demo.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController 
@RequestMapping ("/api/dashboard")
public class DashboardController {
    
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/resume")
    public ResponseEntity<DashboarResumenRequest> obtenerResumen() {
        // Ahora sí estamos usando el servicio para traer los datos reales (o de prueba)
        DashboarResumenRequest resumen = dashboardService.obtenerResumenCompras();
        return ResponseEntity.ok(resumen);
    }
}
