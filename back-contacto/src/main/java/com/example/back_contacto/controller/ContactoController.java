package com.example.back_contacto.controller;

import com.example.back_contacto.dto.ContactoDTO;
import com.example.back_contacto.model.Contacto;
import com.example.back_contacto.service.ContactoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/contactos")
@RequiredArgsConstructor 
public class ContactoController {   

    private final ContactoService contactoService;

    @PostMapping
    public ResponseEntity<Contacto> crear(@Valid @RequestBody ContactoDTO contactoDTO) {
        Contacto nuevo = contactoService.crearContacto(contactoDTO);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Contacto>> listarTodos() {
        return ResponseEntity.ok(contactoService.listarTodos());
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Contacto>> listarPorUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(contactoService.listarPorUsuario(idUsuario));
    }

    @GetMapping("/{idContacto}")
    public ResponseEntity<Contacto> obtenerPorId(@PathVariable Long idContacto) {
        return ResponseEntity.ok(contactoService.obtenerPorId(idContacto));
    }

    @PutMapping("/{idContacto}/validar")
    public ResponseEntity<Contacto> validarCuenta(@PathVariable Long idContacto) {
        return ResponseEntity.ok(contactoService.validarCuenta(idContacto));
    }

    @PutMapping("/{idContacto}")
    public ResponseEntity<Contacto> actualizar(@PathVariable Long idContacto,
                                                @Valid @RequestBody ContactoDTO contactoDTO) {
        return ResponseEntity.ok(contactoService.actualizarContacto(idContacto, contactoDTO));
    }

    @DeleteMapping("/{idContacto}")
    public ResponseEntity<Void> eliminar(@PathVariable Long idContacto) {
        contactoService.eliminarContacto(idContacto);
        return ResponseEntity.noContent().build();
    }

}
