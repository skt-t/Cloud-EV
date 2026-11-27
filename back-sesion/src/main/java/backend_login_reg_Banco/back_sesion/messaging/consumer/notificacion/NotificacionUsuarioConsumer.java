package backend_login_reg_Banco.back_sesion.messaging.consumer.notificacion;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

import backend_login_reg_Banco.back_sesion.dto.UsuarioRegistradoEvent;
import backend_login_reg_Banco.back_sesion.service.NotificacionUsuarioService;

/** Dominio: notificaciones. Consume usuario.registrado desde la cola notificaciones.usuario. */
@Component
public class NotificacionUsuarioConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificacionUsuarioConsumer.class);

    private final NotificacionUsuarioService notificacionService;

    public NotificacionUsuarioConsumer(NotificacionUsuarioService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue-usuario}")
    public void onUsuarioRegistrado(@Payload UsuarioRegistradoEvent evento,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag,
            @Header(AmqpHeaders.REDELIVERED) boolean redelivered) throws IOException {
        try {
            if (evento == null || evento.idUsuario() == null) {
                throw new IllegalArgumentException("El evento no trae idUsuario");
            }
            notificacionService.notificarBienvenida(evento);
            channel.basicAck(tag, false);                               // procesado -> ACK
        } catch (IllegalArgumentException e) {
            // Error NO recuperable (mensaje inválido): reintentar no sirve -> DLQ
            log.error("[NOTIFICACION] Mensaje inválido, enviado a DLQ: {}", e.getMessage());
            channel.basicNack(tag, false, false);
        } catch (Exception e) {
            if (!redelivered) {
                // Error recuperable: se reencola una sola vez
                log.warn("[NOTIFICACION] Fallo temporal, se reintenta una vez: {}", e.getMessage());
                channel.basicNack(tag, false, true);
            } else {
                // Ya se reintentó -> DLQ
                log.error("[NOTIFICACION] Fallo tras reintento, enviado a DLQ: {}", e.getMessage());
                channel.basicNack(tag, false, false);
            }
        }
    }
}
