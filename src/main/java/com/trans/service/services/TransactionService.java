package com.trans.service.services;

import com.trans.service.entities.Transaction;
import com.trans.service.dto.TransactionRequestDto;
import com.trans.service.repositories.TransactionRepository;
import com.trans.service.clients.ContactoClient;
import com.trans.service.dto.ContactoResponseDto;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final ContactoClient contactoClient;

    public Transaction transferir(TransactionRequestDto dto, String rutRemitente, String token) {
        ContactoResponseDto contacto = contactoClient.validarContacto(dto.getIdContacto(), token);

        if (contacto == null) {
            throw new RuntimeException("El contacto no existe o el token no es válido.");
        }

        Transaction t = new Transaction();
        t.setIdUsuario(dto.getIdUsuario());
        t.setRutOrigen(rutRemitente);

        Long idContactoFinal = (contacto.id() != null) ? contacto.id() : dto.getIdContacto();
        t.setIdContacto(idContactoFinal);

        String rutDestinoFinal = (contacto.rut() != null) ? contacto.rut() : dto.getRutDestino();
        t.setRutDestino(rutDestinoFinal);

        t.setMonto(dto.getMonto());

        return transactionRepository.save(t);
    }

    public List<Transaction> obtenerPorUsuario(Long idUsuario) {
        return transactionRepository.findByIdUsuario(idUsuario);
    }
}