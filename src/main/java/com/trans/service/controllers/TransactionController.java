package com.trans.service.controllers;

import org.springframework.http.ResponseEntity;
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

    /**
     * El JWT de Cognito ya fue validado por Spring Security. Con Cognito el "name" del token es un UUID (sub),
     * no un RUT, por eso el RUT de origen se toma del cuerpo de la petición.
     * El token se reenvía a back-contacto para validar el contacto.
     */
    @PostMapping
    public ResponseEntity<Transaction> transferir(
            @Valid @RequestBody TransactionRequestDto dto,
            @RequestHeader("Authorization") String token) {

        Transaction transaction = transactionService.transferir(dto, dto.getRutOrigen(), token);

        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Transaction>> listarPorUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(transactionService.obtenerPorUsuario(idUsuario));
    }
}
