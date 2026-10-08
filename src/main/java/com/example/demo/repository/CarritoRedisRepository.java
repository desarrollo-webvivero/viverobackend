package com.example.demo.repository;

import com.example.demo.model.CarritoRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarritoRedisRepository extends CrudRepository<CarritoRedis, String> {
    // Spring Data Redis provee save(), findById(), deleteById() automáticamente
}