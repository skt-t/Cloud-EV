package com.trans.service.services;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import com.trans.service.repositories.TransactionRepository;
import com.trans.service.entities.Transaction;
import com.trans.service.dto.TransactionRequestDto;
import java.time.LocalDateTime;

    
@Service 
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public Transaction transferir(TransactionRequestDto dto) {
        Transaction transaction = new Transaction();
        transaction.setIdUsuario(dto.getIdUsuario());
        transaction.setRutOrigen(dto.getRutOrigen());
        transaction.setIdContacto(dto.getIdContacto());
        transaction.setRutDestino(dto.getRutDestino());
        transaction.setMonto(dto.getMonto());
        transaction.setFechaTransferencia(LocalDateTime.now());
        transaction.setEstado("PENDIENTE");

        return transactionRepository.save(transaction);
    }
}
