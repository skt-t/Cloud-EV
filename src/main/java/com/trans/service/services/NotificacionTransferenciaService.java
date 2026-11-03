package com.trans.service.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.service.dto.TransferenciaRealizadaEvent;

/** Lógica de la notificación de una transferencia. Aquí iría el envío real (correo, SNS, etc.); se simula con un log. */
@Service
public class NotificacionTransferenciaService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionTransferenciaService.class);

    public void notificar(TransferenciaRealizadaEvent evento) {
        log.info("[NOTIFICACION] Transferencia {} por {} realizada el {}. Aviso enviado al titular.",
                evento.idTransaccion(), evento.monto(), evento.fecha());
    }
}
