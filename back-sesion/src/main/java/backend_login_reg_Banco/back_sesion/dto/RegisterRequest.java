package backend_login_reg_Banco.back_sesion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String nombre;
    private String rut;
    private String gmail;
    private String permisos; // "Admin" o "Usuario"
    private String password; // Añadimos password para la simulación local
}