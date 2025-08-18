package com.cibercom.facturacion_back.controller;

import com.cibercom.facturacion_back.dto.FacturaRequest;
import com.cibercom.facturacion_back.dto.FacturaResponse;
import com.cibercom.facturacion_back.dto.FacturaFrontendRequest;
import com.cibercom.facturacion_back.dto.FacturaConsultaResponse;
import com.cibercom.facturacion_back.service.FacturaService;
import com.cibercom.facturacion_back.service.FacturaConsultaService;
import com.cibercom.facturacion_back.service.SatValidationService;
import com.cibercom.facturacion_back.dto.SatValidationRequest;
import com.cibercom.facturacion_back.dto.SatValidationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.HashMap;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.cibercom.facturacion_back.model.Factura;
import com.cibercom.facturacion_back.repository.FacturaRepository;
import com.cibercom.facturacion_back.model.FacturaMongo;
import com.cibercom.facturacion_back.repository.FacturaMongoRepository;
import java.util.Optional;
import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/factura")
public class FacturaController {

    private static final Logger logger = LoggerFactory.getLogger(FacturaController.class);

    @Autowired
    private FacturaService facturaService;
    
    @Autowired
    private FacturaConsultaService facturaConsultaService;

    @Autowired
    private SatValidationService satValidationService;

    @Autowired
    private Environment environment;

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private FacturaMongoRepository facturaMongoRepository;

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
     * Endpoint para consultar TODAS las facturas sin filtros
     * GET /api/factura/consultar-por-empresa
     */
    @GetMapping("/consultar-por-empresa")
    public ResponseEntity<FacturaConsultaResponse> consultarFacturasPorEmpresa() {
        
        logger.info("=== CONSULTA DE TODAS LAS FACTURAS ===");
        logger.info("Sin filtros - consultando todas las facturas disponibles");
        
        try {
            FacturaConsultaResponse response = facturaConsultaService.consultarFacturasPorEmpresa();
            
            if (response.isExitoso()) {
                logger.info("✅ Consulta exitosa: {} facturas encontradas", response.getTotalFacturas());
                logger.info("Mensaje: {}", response.getMensaje());
            } else {
                logger.error("❌ Error en consulta: {}", response.getError());
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error al consultar facturas", e);
            FacturaConsultaResponse errorResponse = FacturaConsultaResponse.builder()
                .exitoso(false)
                .error("Error al consultar facturas: " + e.getMessage())
                .facturas(new java.util.ArrayList<>())
                .totalFacturas(0)
                .build();
            return ResponseEntity.internalServerError().body(errorResponse);
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

    /**
     * Endpoint para descargar XML de una factura específica
     * GET /api/factura/descargar-xml/{uuid}
     */
    @GetMapping("/descargar-xml/{uuid}")
    public ResponseEntity<?> descargarXmlFactura(@PathVariable String uuid) {
        
        logger.info("=== DESCARGA DE XML ===");
        logger.info("UUID de factura: {}", uuid);
        
        try {
            // Determinar qué base de datos usar según el perfil activo
            String activeProfile = environment.getActiveProfiles().length > 0 ? 
                environment.getActiveProfiles()[0] : "oracle";
            
            String xmlContent = null;
            String nombreArchivo = null;
            
            if ("oracle".equals(activeProfile)) {
                // Buscar en Oracle
                Optional<Factura> facturaOpt = facturaRepository.findByUuid(uuid);
                if (facturaOpt.isPresent()) {
                    Factura factura = facturaOpt.get();
                    xmlContent = factura.getXmlContent();
                    nombreArchivo = "FACTURA_" + factura.getCodigoFacturacion() + ".xml";
                }
            } else if ("mongo".equals(activeProfile)) {
                // Buscar en MongoDB
                FacturaMongo factura = facturaMongoRepository.findByUuid(uuid);
                if (factura != null) {
                    xmlContent = factura.getXmlContent();
                    nombreArchivo = "FACTURA_" + factura.getCodigoFacturacion() + ".xml";
                }
            } else {
                // Buscar en ambas bases
                try {
                    Optional<Factura> facturaOpt = facturaRepository.findByUuid(uuid);
                    if (facturaOpt.isPresent()) {
                        Factura facturaOracle = facturaOpt.get();
                        xmlContent = facturaOracle.getXmlContent();
                        nombreArchivo = "FACTURA_" + facturaOracle.getCodigoFacturacion() + ".xml";
                    }
                } catch (Exception e) {
                    logger.warn("Error buscando en Oracle: {}", e.getMessage());
                }
                
                if (xmlContent == null) {
                    try {
                        FacturaMongo facturaMongo = facturaMongoRepository.findByUuid(uuid);
                        if (facturaMongo != null) {
                            xmlContent = facturaMongo.getXmlContent();
                            nombreArchivo = "FACTURA_" + facturaMongo.getCodigoFacturacion() + ".xml";
                        }
                    } catch (Exception e) {
                        logger.warn("Error buscando en MongoDB: {}", e.getMessage());
                    }
                }
            }
            
            if (xmlContent != null && !xmlContent.trim().isEmpty()) {
                logger.info("✅ XML encontrado para descarga: {}", nombreArchivo);
                
                // Configurar headers para descarga
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_XML);
                headers.setContentDispositionFormData("attachment", nombreArchivo);
                
                return ResponseEntity.ok()
                    .headers(headers)
                    .body(xmlContent);
            } else {
                logger.error("❌ XML no encontrado para UUID: {}", uuid);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("❌ Error al descargar XML", e);
            return ResponseEntity.internalServerError()
                .body("Error al descargar XML: " + e.getMessage());
        }
    }
}
