package com.example.back_contacto.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "contactos")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Contacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idContacto;

    @NotNull(message = "El id de usuario es obligatorio")
    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @NotBlank(message = "El nombre del contacto es obligatorio")
    @Column(name = "nombre_contacto", nullable = false, length = 100)
    private String nombreContacto;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo inválido")
    @Column(name = "gmail", nullable = false)
    private String gmail;

    @NotBlank(message = "El RUT es obligatorio")
    @Pattern(regexp = "^[0-9]{7,8}-[0-9kK]$", message = "Formato de RUT inválido (ej: 12345678-9)")
    @Column(name = "rut", nullable = false, unique = true, length = 12)
    private String rut;

    @Column(name = "cuenta_validada", nullable = false)
    private Boolean cuentaValidada = false;

}
