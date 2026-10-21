package com.example.back_contacto.repository;

import com.example.back_contacto.model.Contacto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ContactoRepository extends JpaRepository<Contacto, Long>{

    List<Contacto> findByIdUsuario(Long idUsuario);

    Optional<Contacto> findByRut(String rut);

    boolean existsByRut(String rut);

}
