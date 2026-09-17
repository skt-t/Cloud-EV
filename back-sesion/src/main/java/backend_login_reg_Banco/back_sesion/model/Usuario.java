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

    // Para los niveles de permiso (Admin o Usuario)
    @Column(nullable = false)
    private String permisos;
    
    // Como el administrador puede autorizar el acceso de un usuario registrado a través del portal,
    // es útil tener un campo para manejar este estado.
    @Column(nullable = false)
    private boolean autorizado;
    
    @Column(nullable = false)
    private String password;
}