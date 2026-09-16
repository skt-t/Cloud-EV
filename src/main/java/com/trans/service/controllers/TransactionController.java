package com.trans.service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;

import com.trans.service.entities.Transaction;
import com.trans.service.dto.TransactionRequestDto;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

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
}
