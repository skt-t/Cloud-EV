package com.trans.service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.trans.service.entities.Transaction;
import com.trans.service.dto.TransactionRequestDto;
import com.trans.service.services.TransactionService;

import java.util.List;

@RestController 
@RequestMapping("/api/transferencias")
@RequiredArgsConstructor 
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<Transaction> transferir(
            @Valid @RequestBody TransactionRequestDto dto,
            @RequestHeader("Authorization") String token) {

        String rutRemitente = SecurityContextHolder.getContext().getAuthentication().getName();
        Transaction transaction = transactionService.transferir(dto, rutRemitente, token);
        
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Transaction>> listarPorUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(transactionService.obtenerPorUsuario(idUsuario));
    }
}