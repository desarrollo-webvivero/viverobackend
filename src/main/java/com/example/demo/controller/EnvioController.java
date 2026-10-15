package com.example.demo.controller;

import com.example.demo.model.Envio;
import com.example.demo.repository.EnvioRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/envios")
@CrossOrigin(origins = "*")
public class EnvioController {

    private final EnvioRepository envioRepository;

    public EnvioController(EnvioRepository envioRepository) {
        this.envioRepository = envioRepository;
    }

    @GetMapping
    public ResponseEntity<List<Envio>> obtenerEnvios() {

        return ResponseEntity.ok(
                envioRepository.findAll()
        );
    }

    @GetMapping("/departamento/{departamento}")
    public ResponseEntity<List<Envio>> obtenerPorDepartamento(
            @PathVariable String departamento) {

        return ResponseEntity.ok(
                envioRepository
                        .findByDepartamentoOrderByMunicipio(
                                departamento
                        )
        );
    }
}