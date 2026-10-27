package com.example.back_contacto.service;

import com.example.back_contacto.messaging.event.ContactoEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Lógica de la notificación de contacto. Aquí iría el envío real (correo, SNS, etc.); se simula con un log. */
@Slf4j
@Service
public class NotificacionContactoService {

    public void notificarContactoCreado(ContactoEvent evento) {
        log.info("[NOTIFICACION] Contacto '{}' creado. Aviso enviado a {} (idContacto={})",
                evento.getNombreContacto(), evento.getGmail(), evento.getIdContacto());
    }
}
