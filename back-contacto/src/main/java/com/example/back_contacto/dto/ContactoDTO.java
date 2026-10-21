package com.example.back_contacto.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ContactoDTO {

    @NotNull(message = "El id de usuario es obligatorio")
    private Long idUsuario;

    @NotBlank(message = "El nombre del contacto es obligatorio")
    private String nombreContacto;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo inválido")
    private String gmail;

    @NotBlank(message = "El RUT es obligatorio")
    @Pattern(regexp = "^[0-9]{7,8}-[0-9kK]$", message = "Formato de RUT inválido (ej: 12345678-9)")
    private String rut;
}