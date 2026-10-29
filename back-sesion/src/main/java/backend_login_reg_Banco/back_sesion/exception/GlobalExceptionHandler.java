package backend_login_reg_Banco.back_sesion.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduce los errores a respuestas JSON claras: {"error": "CODIGO", "message": "..."}. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsuarioDuplicadoException.class)
    public ResponseEntity<Map<String, Object>> duplicado(UsuarioDuplicadoException e) {
        return cuerpo(HttpStatus.CONFLICT, e.getCodigo(), e.getCodigo());
    }

    // Respaldo: dos registros simultáneos pueden pasar la verificación y chocar en el UNIQUE de la BD.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> integridad(DataIntegrityViolationException e) {
        return cuerpo(HttpStatus.CONFLICT, "DUPLICADO", "DUPLICADO: el RUT o el correo ya están registrados");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> invalido(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(f -> campos.putIfAbsent(f.getField(), f.getDefaultMessage()));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "DATOS_INVALIDOS");
        body.put("message", "DATOS_INVALIDOS: revisa los campos enviados");
        body.put("campos", campos);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> cuerpoIlegible(HttpMessageNotReadableException e) {
        return cuerpo(HttpStatus.BAD_REQUEST, "CUERPO_INVALIDO", "CUERPO_INVALIDO: el JSON enviado no se puede leer");
    }

    private ResponseEntity<Map<String, Object>> cuerpo(HttpStatus status, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", error);
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
