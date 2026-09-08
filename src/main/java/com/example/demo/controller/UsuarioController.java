package com.example.demo.controller;

import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/usuarios")
@CrossOrigin(origins ="*")
public class UsuarioController{

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public List<Usuario> obtenerUsuarios(){
        return usuarioRepository.findAll();
    }

    @PostMapping 
    public Usuario guardarUsuario(@RequestBody Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
}