package backend_login_reg_Banco.back_sesion.repository;

import backend_login_reg_Banco.back_sesion.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByRut(String rut);
    Optional<Usuario> findByGmail(String gmail);
}