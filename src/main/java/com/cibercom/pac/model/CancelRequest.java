package com.cibercom.pac.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;

@Entity
@Table(name = "cancel_requests")
@Getter
@Setter
public class CancelRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String uuid;

    @Column(nullable = false, length = 2)
    private String motivo;

    @Column(nullable = false, length = 13)
    private String rfcEmisor;

    @Column(nullable = false, length = 13)
    private String rfcReceptor;

    private Double total;

    @Column(length = 20)
    private String tipo; // INGRESO, EGRESO, NOMINA, TRASLADO

    private OffsetDateTime fechaFactura;

    private Boolean publicoGeneral;

    private Boolean tieneRelaciones;

    @Column(length = 64)
    private String uuidSustituto;

    @Column(length = 20)
    private String status; // EN_PROCESO, CANCELADA, RECHAZADA

    @Column(length = 32)
    private String receiptId;

    private OffsetDateTime decidedAt;

    @Column(length = 64)
    private String resultCode; // 0, RECHAZADA_RECEPTOR, FUERA_VENTANA, etc.

    @Column(length = 255)
    private String resultMessage;

    private OffsetDateTime createdAt = OffsetDateTime.now();
}


