package com.example.back_contacto.service;


import com.example.back_contacto.dto.ContactoDTO;
import com.example.back_contacto.model.Contacto;
import com.example.back_contacto.repository.ContactoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor  
public class ContactoService {
    private final ContactoRepository contactoRepository;

    public Contacto crearContacto(ContactoDTO dto) {
        if (contactoRepository.existsByRut(dto.getRut())) {
            throw new RuntimeException("Ya existe un contacto con ese RUT");
        }
        
        Contacto contacto = new Contacto();
        contacto.setIdUsuario(dto.getIdUsuario());
        contacto.setNombreContacto(dto.getNombreContacto());
        contacto.setGmail(dto.getGmail());
        contacto.setRut(dto.getRut());
        contacto.setCuentaValidada(false); // Forzamos el estado por defecto
        
        return contactoRepository.save(contacto);
    }

    public List<Contacto> listarTodos() {
        return contactoRepository.findAll();
    }

    public List<Contacto> listarPorUsuario(Long idUsuario) {
        return contactoRepository.findByIdUsuario(idUsuario);
    }

    public Contacto obtenerPorId(Long idContacto) {
        return contactoRepository.findById(idContacto)
                .orElseThrow(() -> new RuntimeException("Contacto no encontrado con id: " + idContacto));
    }

    public Contacto validarCuenta(Long idContacto) {
        Contacto contacto = obtenerPorId(idContacto);
        contacto.setCuentaValidada(true);
        return contactoRepository.save(contacto);
    }

    public Contacto actualizarContacto(Long idContacto, ContactoDTO datosNuevos) {
        Contacto contacto = obtenerPorId(idContacto);
        contacto.setNombreContacto(datosNuevos.getNombreContacto());
        contacto.setGmail(datosNuevos.getGmail());
        contacto.setRut(datosNuevos.getRut());
        // No permitimos actualizar el idUsuario ni el estado de validación desde aquí
        return contactoRepository.save(contacto);
    }

    public void eliminarContacto(Long idContacto) {
        Contacto contacto = obtenerPorId(idContacto);
        contactoRepository.delete(contacto);
    }

    

}
