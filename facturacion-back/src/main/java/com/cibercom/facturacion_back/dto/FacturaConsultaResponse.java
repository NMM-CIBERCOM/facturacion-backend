package com.cibercom.facturacion_back.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacturaConsultaResponse {
    
    private boolean exitoso;
    private String mensaje;
    private List<FacturaConsultaDTO> facturas;
    private int totalFacturas;
    private String error;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacturaConsultaDTO {
        private String uuid;
        private String codigoFacturacion;
        private String tienda;
        private LocalDateTime fechaFactura;
        private String terminal;
        private String boleta;
        private String razonSocial;
        private String rfc;
        private BigDecimal total;
        private String estado;
        private String medioPago;
        private String formaPago;
        private LocalDateTime fechaGeneracion;
        private LocalDateTime fechaTimbrado;
        private BigDecimal subtotal;
        private BigDecimal iva;
        private BigDecimal ieps;
    }
}
