package com.trans.service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;

import com.trans.service.entities.Transaction;
import com.trans.service.dto.TransactionRequestDto;

import java.util.List;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController 
@RequestMapping ("/api/transferencias")
@RequiredArgsConstructor 
public class TransactionController {

    private final com.trans.service.services.TransactionService transactionService;

    @PostMapping
    public ResponseEntity<Transaction> transferir(@Valid @RequestBody TransactionRequestDto dto) {
        Transaction transaction = transactionService.transferir(dto);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Transaction>> listarPorUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(transactionService.obtenerPorUsuario(idUsuario));
    }
    
}
