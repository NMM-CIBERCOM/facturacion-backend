package com.cibercom.facturacion_back.service;

import com.cibercom.facturacion_back.dto.FacturaConsultaResponse;
import com.cibercom.facturacion_back.model.Factura;
import com.cibercom.facturacion_back.model.FacturaMongo;
import com.cibercom.facturacion_back.repository.FacturaRepository;
import com.cibercom.facturacion_back.repository.FacturaMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FacturaConsultaService {

    @Autowired
    private FacturaRepository facturaRepository;
    
    @Autowired
    private FacturaMongoRepository facturaMongoRepository;
    
    @Autowired
    private Environment environment;

    /**
     * Consulta TODAS las facturas sin ningún filtro
     * Consulta la base de datos configurada según el perfil activo
     */
    public FacturaConsultaResponse consultarFacturasPorEmpresa() {
        try {
            List<FacturaConsultaResponse.FacturaConsultaDTO> facturas = new ArrayList<>();
            
            // Determinar qué base de datos usar según el perfil activo
            String activeProfile = getActiveProfile();
            System.out.println("🔍 Perfil activo detectado: " + activeProfile);
            
            if ("oracle".equals(activeProfile)) {
                // Consultar solo en Oracle - TODAS las facturas
                System.out.println("🗄️ Consultando solo Oracle...");
                List<Factura> facturasOracle = consultarFacturasOracle();
                List<FacturaConsultaResponse.FacturaConsultaDTO> facturasOracleDTO = convertirFacturasOracle(facturasOracle);
                facturas.addAll(facturasOracleDTO);
                System.out.println("✅ Oracle: " + facturasOracleDTO.size() + " facturas añadidas");
            } else if ("mongo".equals(activeProfile)) {
                // Consultar solo en MongoDB - TODAS las facturas
                System.out.println("🗄️ Consultando solo MongoDB...");
                List<FacturaMongo> facturasMongo = consultarFacturasMongo();
                List<FacturaConsultaResponse.FacturaConsultaDTO> facturasMongoDTO = convertirFacturasMongo(facturasMongo);
                facturas.addAll(facturasMongoDTO);
                System.out.println("✅ MongoDB: " + facturasMongoDTO.size() + " facturas añadidas");
            } else {
                // Perfil por defecto o híbrido - consultar ambas bases
                System.out.println("⚠️ PERFIL HÍBRIDO - Consultando ambas bases de datos");
                try {
                    List<Factura> facturasOracle = consultarFacturasOracle();
                    List<FacturaConsultaResponse.FacturaConsultaDTO> facturasOracleDTO = convertirFacturasOracle(facturasOracle);
                    facturas.addAll(facturasOracleDTO);
                    System.out.println("✅ Oracle: " + facturasOracleDTO.size() + " facturas añadidas");
                } catch (Exception e) {
                    System.err.println("❌ Error consultando Oracle: " + e.getMessage());
                }
                
                try {
                    List<FacturaMongo> facturasMongo = consultarFacturasMongo();
                    List<FacturaConsultaResponse.FacturaConsultaDTO> facturasMongoDTO = convertirFacturasMongo(facturasMongo);
                    facturas.addAll(facturasMongoDTO);
                    System.out.println("✅ MongoDB: " + facturasMongoDTO.size() + " facturas añadidas");
                } catch (Exception e) {
                    System.err.println("❌ Error consultando MongoDB: " + e.getMessage());
                }
            }

            System.out.println("📊 TOTAL FINAL: " + facturas.size() + " facturas");
            System.out.println("🔍 UUIDs únicos: " + facturas.stream().map(f -> f.getUuid()).distinct().count());

            return FacturaConsultaResponse.builder()
                .exitoso(true)
                .mensaje("Todas las facturas consultadas exitosamente desde " + activeProfile.toUpperCase())
                .facturas(facturas)
                .totalFacturas(facturas.size())
                .build();
                
        } catch (Exception e) {
            return FacturaConsultaResponse.builder()
                .exitoso(false)
                .error("Error al consultar facturas: " + e.getMessage())
                .facturas(new ArrayList<>())
                .totalFacturas(0)
                .build();
        }
    }

    /**
     * Obtiene el perfil activo de Spring
     */
    private String getActiveProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        System.out.println("🔍 Perfiles activos detectados: " + java.util.Arrays.toString(activeProfiles));
        if (activeProfiles.length > 0) {
            String profile = activeProfiles[0];
            System.out.println("✅ Perfil seleccionado: " + profile);
            return profile;
        }
        System.out.println("⚠️ No se detectaron perfiles activos, usando 'oracle' por defecto");
        return "oracle"; // Perfil por defecto
    }

    /**
     * Consulta TODAS las facturas en Oracle sin filtros
     */
    private List<Factura> consultarFacturasOracle() {
        List<Factura> facturas = new ArrayList<>();
        
        try {
            // Consultar TODAS las facturas sin ningún filtro
            facturas = facturaRepository.findAll();
            System.out.println("Oracle: " + facturas.size() + " facturas encontradas");
        } catch (Exception e) {
            System.err.println("Error en consulta Oracle: " + e.getMessage());
            facturas = new ArrayList<>();
        }
        
        return facturas;
    }

    /**
     * Consulta TODAS las facturas en MongoDB sin filtros
     */
    private List<FacturaMongo> consultarFacturasMongo() {
        List<FacturaMongo> facturas = new ArrayList<>();
        
        try {
            // Consultar TODAS las facturas sin ningún filtro
            facturas = facturaMongoRepository.findAll();
            System.out.println("MongoDB: " + facturas.size() + " facturas encontradas");
        } catch (Exception e) {
            System.err.println("Error en consulta MongoDB: " + e.getMessage());
            facturas = new ArrayList<>();
        }
        
        return facturas;
    }

    /**
     * Convierte entidades Factura de Oracle a DTOs
     */
    private List<FacturaConsultaResponse.FacturaConsultaDTO> convertirFacturasOracle(List<Factura> facturasOracle) {
        return facturasOracle.stream()
            .map(f -> FacturaConsultaResponse.FacturaConsultaDTO.builder()
                .uuid(f.getUuid())
                .codigoFacturacion(f.getCodigoFacturacion())
                .tienda(f.getTienda())
                .fechaFactura(f.getFechaGeneracion())
                .terminal(f.getTerminal())
                .boleta(f.getBoleta())
                .razonSocial(f.getReceptorRazonSocial())
                .rfc(f.getReceptorRfc())
                .total(f.getTotal())
                .estado(f.getEstado())
                .medioPago(f.getMedioPago())
                .formaPago(f.getFormaPago())
                .fechaGeneracion(f.getFechaGeneracion())
                .fechaTimbrado(f.getFechaTimbrado())
                .subtotal(f.getSubtotal())
                .iva(f.getIva())
                .ieps(f.getIeps())
                .build())
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Convierte entidades FacturaMongo a DTOs
     */
    private List<FacturaConsultaResponse.FacturaConsultaDTO> convertirFacturasMongo(List<FacturaMongo> facturasMongo) {
        return facturasMongo.stream()
            .map(f -> FacturaConsultaResponse.FacturaConsultaDTO.builder()
                .uuid(f.getUuid())
                .codigoFacturacion(f.getCodigoFacturacion())
                .tienda(f.getTienda())
                .fechaFactura(f.getFechaFactura())
                .terminal(f.getTerminal())
                .boleta(f.getBoleta())
                .razonSocial(f.getReceptor() != null ? (String) f.getReceptor().get("razonSocial") : "")
                .rfc(f.getReceptor() != null ? (String) f.getReceptor().get("rfc") : "")
                .total(f.getTotal())
                .estado(f.getEstado())
                .medioPago(f.getMedioPago())
                .formaPago(f.getFormaPago())
                .fechaGeneracion(f.getFechaGeneracion())
                .fechaTimbrado(f.getFechaTimbrado())
                .subtotal(f.getSubtotal())
                .iva(f.getIva())
                .ieps(f.getIeps())
                .build())
            .collect(java.util.stream.Collectors.toList());
    }
}
