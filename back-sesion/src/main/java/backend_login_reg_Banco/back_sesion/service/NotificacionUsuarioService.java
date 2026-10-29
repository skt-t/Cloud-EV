package backend_login_reg_Banco.back_sesion.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import backend_login_reg_Banco.back_sesion.dto.UsuarioRegistradoEvent;

/** Lógica de la notificación de bienvenida. Aquí iría el envío real (correo, SNS, etc.); se simula con un log. */
@Service
public class NotificacionUsuarioService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionUsuarioService.class);

    public void notificarBienvenida(UsuarioRegistradoEvent evento) {
        log.info("[NOTIFICACION] Bienvenida para el usuario {} ({}) registrado el {}. Aviso enviado.",
                evento.idUsuario(), evento.gmail(), evento.fecha());
    }
}
