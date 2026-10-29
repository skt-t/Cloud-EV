package com.admin.service.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.admin.service.dto.BindingRequest;
import com.admin.service.dto.ExchangeRequest;
import com.admin.service.dto.QueueRequest;
import com.admin.service.service.RabbitAdminService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class RabbitAdminController {

    private final RabbitAdminService service;

    public RabbitAdminController(RabbitAdminService service) {
        this.service = service;
    }

    @PostMapping("/queues")
    public ResponseEntity<Map<String, Object>> crearCola(@Valid @RequestBody QueueRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearCola(r));
    }

    @GetMapping("/queues")
    public List<Map<String, Object>> listarColas() {
        return service.listarColas();
    }

    @DeleteMapping("/queues/{name}")
    public ResponseEntity<Void> eliminarCola(@PathVariable String name) {
        service.eliminarCola(name);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/exchanges")
    public ResponseEntity<Map<String, Object>> crearExchange(@Valid @RequestBody ExchangeRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearExchange(r));
    }

    @GetMapping("/exchanges")
    public List<Map<String, Object>> listarExchanges() {
        return service.listarExchanges();
    }

    @DeleteMapping("/exchanges/{name}")
    public ResponseEntity<Void> eliminarExchange(@PathVariable String name) {
        service.eliminarExchange(name);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bindings")
    public ResponseEntity<Map<String, Object>> crearBinding(@Valid @RequestBody BindingRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearBinding(r));
    }

    @GetMapping("/bindings")
    public List<Map<String, Object>> listarBindings() {
        return service.listarBindings();
    }

    @DeleteMapping("/bindings")
    public ResponseEntity<Void> eliminarBinding(@Valid @RequestBody BindingRequest r) {
        service.eliminarBinding(r);
        return ResponseEntity.noContent().build();
    }
}
