package com.trans.service.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.trans.service.entities.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByIdUsuario(Long idUsuario);

}