package backend_login_reg_Banco.back_sesion.controller;

import backend_login_reg_Banco.back_sesion.model.Usuario;
import backend_login_reg_Banco.back_sesion.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

     private final UsuarioRepository usuarioRepository;

     @GetMapping("/{id}")
     public ResponseEntity<Usuario> obtenerUsuario(@PathVariable Long id) {
         return usuarioRepository.findById(id)
                 .map(ResponseEntity::ok)
                 .orElse(ResponseEntity.notFound().build());
     }

     @GetMapping("/buscar")
     public ResponseEntity<Usuario> buscarUsuario(
             @RequestParam(required = false) String rut,
             @RequestParam(required = false) String gmail) {

         Optional<Usuario> usuario = Optional.empty();

         if (rut != null && !rut.isBlank()) {
             usuario = usuarioRepository.findByRut(rut);
         } else if (gmail != null && !gmail.isBlank()) {
             usuario = usuarioRepository.findByGmail(gmail);
         }

         return usuario.map(ResponseEntity::ok)
                 .orElse(ResponseEntity.notFound().build());
     }
}