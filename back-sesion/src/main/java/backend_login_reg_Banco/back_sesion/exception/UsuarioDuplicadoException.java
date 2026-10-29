package backend_login_reg_Banco.back_sesion.exception;

/** RUT o correo ya registrados. El código ("RUT_DUPLICADO" / "GMAIL_DUPLICADO") viaja al cliente. */
public class UsuarioDuplicadoException extends RuntimeException {

    private final String codigo;

    public UsuarioDuplicadoException(String codigo) {
        super(codigo);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
