package com.example.back_contacto.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** Mensaje que viaja por RabbitMQ cuando un contacto se crea o se valida. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactoEvent {

    private String eventId;
    private String tipo;          // CONTACTO_CREADO | CONTACTO_VALIDADO
    private Long idContacto;
    private Long idUsuario;
    private String nombreContacto;
    private String gmail;
    private String rut;
    private Instant fecha;
}
