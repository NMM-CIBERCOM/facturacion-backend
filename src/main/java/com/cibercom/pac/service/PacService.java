package com.cibercom.pac.service;

import com.cibercom.pac.model.CancelRequest;
import com.cibercom.pac.repo.CancelRequestRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PacService {

    private final CancelRequestRepository repo;
    private static final Logger logger = LoggerFactory.getLogger(PacService.class);

    public PacService(CancelRequestRepository repo) {
        this.repo = repo;
    }

    private boolean dentroVentanaFiscal(OffsetDateTime fecha) {
        if (fecha == null) return true;
        OffsetDateTime now = OffsetDateTime.now();
        int yF = fecha.getYear();
        int yN = now.getYear();
        int mN = now.getMonthValue();
        if (yF == yN) return true;
        return (yF == yN - 1) && (mN == 1);
    }

    private boolean sinAceptacion(Double total, String tipo, OffsetDateTime fechaFactura, Boolean publicoGeneral) {
        if (Boolean.TRUE.equals(publicoGeneral)) return true;
        if (total != null && total <= 1000.0) return true;
        String t = tipo == null ? "INGRESO" : tipo.toUpperCase();
        if (t.equals("NOMINA") || t.equals("EGRESO") || t.equals("TRASLADO")) return true;
        if (fechaFactura != null) {
            long hours = java.time.Duration.between(fechaFactura, OffsetDateTime.now()).toHours();
            if (hours <= 36) return true; // día hábil siguiente aprox
        }
        return false;
    }

    @Transactional
    public CancelRequest solicitarCancelacion(CancelRequest req) {
        logger.info("PAC: solicitud recibida uuid={}, motivo={}, total={}, tipo={}, fecha={}",
                req.getUuid(), req.getMotivo(), req.getTotal(), req.getTipo(), req.getFechaFactura());
        if (!dentroVentanaFiscal(req.getFechaFactura())) {
            req.setStatus("RECHAZADA");
            req.setResultCode("FUERA_VENTANA_FISCAL");
            req.setResultMessage("Fuera de ventana fiscal");
            logger.info("PAC: uuid={} RECHAZADA por ventana fiscal", req.getUuid());
            return repo.save(req);
        }
        if ("01".equals(req.getMotivo()) && (req.getUuidSustituto() == null || req.getUuidSustituto().isBlank())) {
            req.setStatus("RECHAZADA");
            req.setResultCode("REQUIERE_SUSTITUTO");
            req.setResultMessage("Motivo 01 requiere UUID sustituto");
            logger.info("PAC: uuid={} RECHAZADA por falta de UUID sustituto", req.getUuid());
            return repo.save(req);
        }
        if (Boolean.TRUE.equals(req.getTieneRelaciones()) && !"01".equals(req.getMotivo())) {
            req.setStatus("RECHAZADA");
            req.setResultCode("REQUIERE_01");
            req.setResultMessage("CFDI con relaciones requiere motivo 01 con sustitución");
            logger.info("PAC: uuid={} RECHAZADA por relaciones sin motivo 01", req.getUuid());
            return repo.save(req);
        }

        req.setReceiptId(UUID.randomUUID().toString().substring(0, 12));

        if (sinAceptacion(req.getTotal(), req.getTipo(), req.getFechaFactura(), req.getPublicoGeneral())) {
            req.setStatus("CANCELADA");
            req.setDecidedAt(OffsetDateTime.now());
            req.setResultCode("0");
            req.setResultMessage("Cancelación inmediata");
            logger.info("PAC: uuid={} CANCELADA inmediata (sin aceptación)", req.getUuid());
        } else {
            req.setStatus("EN_PROCESO");
            logger.info("PAC: uuid={} marcada EN_PROCESO (requiere aceptación)", req.getUuid());
        }
        return repo.save(req);
    }

    public CancelRequest obtenerEstado(String uuid) {
        return repo.findTopByUuidOrderByIdDesc(uuid).orElse(null);
    }

    // Resolución asíncrona cada 5s de solicitudes EN_PROCESO
    @Scheduled(fixedDelay = 15000)
    @Transactional
    public void resolverSolicitudes() {
        List<CancelRequest> pendientes = repo.findAll().stream()
            .filter(r -> "EN_PROCESO".equals(r.getStatus()))
            .toList();
        if (!pendientes.isEmpty()) {
            logger.info("PAC: resolviendo {} solicitudes EN_PROCESO", pendientes.size());
        }
        for (CancelRequest r : pendientes) {
            boolean aprobado = Math.random() < 0.8;
            r.setStatus(aprobado ? "CANCELADA" : "RECHAZADA");
            r.setDecidedAt(OffsetDateTime.now());
            r.setResultCode(aprobado ? "0" : "RECHAZADA_RECEPTOR");
            r.setResultMessage(aprobado ? "Cancelación aceptada por receptor" : "Receptor rechazó la cancelación");
            repo.save(r);
            logger.info("PAC: uuid={} resuelta a {}", r.getUuid(), r.getStatus());
            // Callback al backend para persistir estado final
            try {
                var url = System.getenv().getOrDefault("BACKEND_BASE_URL", "http://localhost:8080") + 
                          "/api/consulta-facturas/cancelacion/callback";
                var body = "{\"uuid\":\"" + r.getUuid() + "\",\"status\":\"" + r.getStatus() + "\"}";
                java.net.http.HttpClient.newHttpClient().send(
                    java.net.http.HttpRequest.newBuilder(java.net.URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(body))
                        .build(),
                    java.net.http.HttpResponse.BodyHandlers.ofString()
                );
                logger.info("PAC: callback enviado a backend para uuid={} estado={}", r.getUuid(), r.getStatus());
            } catch (Exception ignored) {}
        }
    }
}


