package com.trans.service.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.PrePersist;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transferencias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTransferencia;

    @Column (name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column (name = "rut_origen", nullable = false)
    private String rutOrigen;

    @Column (name = "id_contacto", nullable = false)
    private Long idContacto;

    @Column (name = "rut_destino", nullable = false)
    private String rutDestino;

    @Column (name = "monto", nullable = false)
    private BigDecimal monto;

    @Column (name = "fecha_transferencia", nullable = false)
    private LocalDateTime fechaTransferencia;

    @Column (name = "estado", nullable = false)
    private String estado;

    @PrePersist
    public void prePersist() {
        if (this.fechaTransferencia == null) {
            this.fechaTransferencia = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = "EXITOSA";
        }
    }
}