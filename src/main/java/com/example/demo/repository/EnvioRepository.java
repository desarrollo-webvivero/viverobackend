package com.example.demo.repository;

import com.example.demo.model.Envio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnvioRepository
        extends JpaRepository<Envio, Long> {

    List<Envio> findByDepartamentoOrderByMunicipio(
            String departamento);

    Optional<Envio> findByDepartamentoAndMunicipio(
            String departamento,
            String municipio);
}