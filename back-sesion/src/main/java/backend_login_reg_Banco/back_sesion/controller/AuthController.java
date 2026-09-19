package backend_login_reg_Banco.back_sesion.controller;

import backend_login_reg_Banco.back_sesion.dto.LoginRequest;
import backend_login_reg_Banco.back_sesion.dto.RegisterRequest;
import backend_login_reg_Banco.back_sesion.model.Usuario;
import backend_login_reg_Banco.back_sesion.repository.UsuarioRepository;
import backend_login_reg_Banco.back_sesion.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UsuarioRepository usuarioRepository;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest request) {
        String token = authService.register(request);
        
        Usuario user = usuarioRepository.findByRut(request.getRut()).orElseThrow();
        
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("idUsuario", String.valueOf(user.getIdUsuario()));
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        String token = authService.login(request);
        
        Usuario user = usuarioRepository.findByRut(request.getRut()).orElseThrow();
        
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("idUsuario", String.valueOf(user.getIdUsuario()));
        
        return ResponseEntity.ok(response);
    }
}