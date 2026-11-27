package backend_login_reg_Banco.back_sesion.service;

import backend_login_reg_Banco.back_sesion.dto.RegisterRequest;
import backend_login_reg_Banco.back_sesion.exception.UsuarioDuplicadoException;
import backend_login_reg_Banco.back_sesion.messaging.UsuarioEventPublisher;
import backend_login_reg_Banco.back_sesion.model.Usuario;
import backend_login_reg_Banco.back_sesion.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioEventPublisher eventPublisher;

    /**
     * Guarda el perfil del usuario (la contraseña ya quedó en Cognito) y publica el evento
     * usuario.registrado. Si RabbitMQ falla, el registro igual se completa.
     */
    public Usuario register(RegisterRequest request) {
        if (usuarioRepository.existsByRut(request.getRut())) {
            throw new UsuarioDuplicadoException("RUT_DUPLICADO");
        }
        if (usuarioRepository.existsByGmail(request.getGmail())) {
            throw new UsuarioDuplicadoException("GMAIL_DUPLICADO");
        }

        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nombre(request.getNombre())
                .rut(request.getRut())
                .gmail(request.getGmail())
                .permisos("Usuario")   // el cliente nunca decide su nivel de permiso
                .autorizado(false)     // requiere validación del Admin
                .build());

        eventPublisher.publicar(usuario);
        return usuario;
    }
}
