package com.trans.service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferenciaRealizadaEvent(
        Long idTransaccion,
        BigDecimal monto,
        LocalDateTime fecha) {
}
