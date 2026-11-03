package com.trans.service.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.service.dto.TransferenciaRealizadaEvent;

/** Lógica de generación del comprobante (ticket) de una transferencia. Se simula con un log. */
@Service
public class ComprobanteService {

    private static final Logger log = LoggerFactory.getLogger(ComprobanteService.class);

    public void generar(TransferenciaRealizadaEvent evento) {
        log.info("[COMPROBANTE] Comprobante generado para la transferencia {} (monto {}).",
                evento.idTransaccion(), evento.monto());
    }
}
