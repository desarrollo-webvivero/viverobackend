package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController 
@RequestMapping ("/api/ping")
public class HealthController {

    @GetMapping
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("OK");
    }
}
