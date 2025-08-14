package com.cibercom.facturacion_back.controller;

import com.cibercom.facturacion_back.dto.FacturaRequest;
import com.cibercom.facturacion_back.dto.FacturaResponse;
import com.cibercom.facturacion_back.dto.FacturaFrontendRequest;
import com.cibercom.facturacion_back.service.FacturaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/factura")
public class FacturaController {

    private static final Logger logger = LoggerFactory.getLogger(FacturaController.class);

    @Autowired
    private FacturaService facturaService;

    /**
     * Endpoint para generar y timbrar una factura
     * POST /api/factura/generar
     */
    @PostMapping("/generar")
    public ResponseEntity<FacturaResponse> generarYTimbrarFactura(
            @Valid @RequestBody FacturaRequest request) {
        
        logger.info("Generando y timbrando factura para: {}", request.getNombreReceptor());
        
        FacturaResponse response = facturaService.generarYTimbrarFactura(request);
        
        if (response.isExitoso()) {
            logger.info("Factura generada exitosamente con UUID: {}", response.getUuid());
            return ResponseEntity.ok(response);
        } else {
            logger.error("Error al generar factura: {}", response.getErrores());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Endpoint para procesar el formulario del frontend
     * POST /api/factura/procesar-frontend
     */
    @PostMapping("/procesar-frontend")
    public ResponseEntity<Map<String, Object>> procesarFormularioFrontend(
            @Valid @RequestBody FacturaFrontendRequest request) {
        
        logger.info("Procesando formulario del frontend para: {}", request.getRazonSocial());
        
        Map<String, Object> response = facturaService.procesarFormularioFrontend(request);
        
        if (Boolean.TRUE.equals(response.get("exitoso"))) {
            logger.info("Formulario procesado exitosamente con UUID: {}", response.get("uuid"));
            return ResponseEntity.ok(response);
        } else {
            logger.error("Error al procesar formulario: {}", response.get("errores"));
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Endpoint para consultar facturas
     * GET /api/factura/consultar
     */
    @GetMapping("/consultar")
    public ResponseEntity<Map<String, Object>> consultarFacturas() {
        logger.info("Consultando facturas almacenadas");
        
        try {
            // Aquí puedes implementar la lógica de consulta
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Consulta de facturas implementada");
            response.put("totalFacturas", 0); // Implementar contador real
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al consultar facturas", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al consultar facturas: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
