package backend_login_reg_Banco.back_sesion.controller;

import backend_login_reg_Banco.back_sesion.dto.RegisterRequest;
import backend_login_reg_Banco.back_sesion.model.Usuario;
import backend_login_reg_Banco.back_sesion.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** Único endpoint público. El login lo hace Cognito, por eso aquí ya no existe /auth/login. */
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        Usuario usuario = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "idUsuario", String.valueOf(usuario.getIdUsuario()),
                "mensaje", "Usuario registrado"));
    }
}
