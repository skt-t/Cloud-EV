package com.trans.service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.trans.service.entities.Transaction;

@Repository 
public interface TransactionRepository extends org.springframework.data.jpa.repository.JpaRepository<com.trans.service.entities.Transaction, Long>{

}
    