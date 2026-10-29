package backend_login_reg_Banco.back_sesion.dto;

import java.time.LocalDateTime;

/** Evento que se publica en RabbitMQ cuando se registra un usuario (routing key usuario.registrado). */
public record UsuarioRegistradoEvent(
        Long idUsuario,
        String nombre,
        String gmail,
        LocalDateTime fecha) {
}
