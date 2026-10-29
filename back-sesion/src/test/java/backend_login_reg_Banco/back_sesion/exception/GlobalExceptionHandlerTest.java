package backend_login_reg_Banco.back_sesion.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void duplicadoRespondeConflictConElCodigo() {
        ResponseEntity<Map<String, Object>> r = handler.duplicado(new UsuarioDuplicadoException("RUT_DUPLICADO"));

        assertEquals(HttpStatus.CONFLICT, r.getStatusCode());
        assertEquals("RUT_DUPLICADO", r.getBody().get("error"));
        assertEquals("RUT_DUPLICADO", r.getBody().get("message"));
    }
}
