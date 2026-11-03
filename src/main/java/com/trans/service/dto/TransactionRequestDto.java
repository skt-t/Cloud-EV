package com.trans.service.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Data 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class TransactionRequestDto {

    @NotNull(message = "El idUsuario no puede ser nulo")
    private Long idUsuario;

    @NotBlank (message = "El rutOrigen no puede estar vacío")
    private String rutOrigen;

    @NotNull(message = "El idContacto no puede ser nulo")
    private Long idContacto;

    @NotBlank (message = "El rutDestino no puede estar vacío")
    private String rutDestino;

    @NotNull(message = "El monto no puede ser nulo")
    @Positive(message = "El monto debe ser un valor positivo")
    private BigDecimal monto;

}
