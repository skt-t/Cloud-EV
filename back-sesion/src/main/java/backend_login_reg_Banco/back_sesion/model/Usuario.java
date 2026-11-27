package backend_login_reg_Banco.back_sesion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Perfil del usuario en MySQL. Las credenciales viven solo en Cognito: aquí no se guarda ninguna contraseña. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String rut;

    @Column(name = "gmail", nullable = false, unique = true)
    private String gmail;

    // Nivel de permiso: "Admin" o "Usuario". Siempre nace como "Usuario"; el Admin se asigna a mano en la BD.
    @Column(nullable = false)
    private String permisos;

    // El administrador puede autorizar el acceso de un usuario registrado.
    @Column(nullable = false)
    private boolean autorizado;
}
