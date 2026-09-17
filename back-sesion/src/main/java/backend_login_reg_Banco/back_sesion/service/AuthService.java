package backend_login_reg_Banco.back_sesion.service;

import backend_login_reg_Banco.back_sesion.dto.LoginRequest;
import backend_login_reg_Banco.back_sesion.dto.RegisterRequest;
import backend_login_reg_Banco.back_sesion.model.Usuario;
import backend_login_reg_Banco.back_sesion.repository.UsuarioRepository;
import backend_login_reg_Banco.back_sesion.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public String register(RegisterRequest request) {
        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .rut(request.getRut())
                .gmail(request.getGmail())
                .permisos(request.getPermisos() != null ? request.getPermisos() : "Usuario")
                .password(passwordEncoder.encode(request.getPassword()))
                .autorizado(false) // Requiere validación del Admin según el caso
                .build();

        usuarioRepository.save(usuario);
        return jwtService.generateToken(usuario);
    }

    public String login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByRut(request.getRut())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        return jwtService.generateToken(usuario);
    }
}