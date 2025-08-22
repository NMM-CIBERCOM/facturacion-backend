package com.cibercom.pac.web;

import com.cibercom.pac.model.CancelRequest;
import com.cibercom.pac.service.PacService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pac")
@CrossOrigin(origins = "*")
public class PacController {

    private final PacService service;

    public PacController(PacService service) {
        this.service = service;
    }

    static class PacResult {
        public boolean ok;
        public String status;
        public String receiptId;
        public String message;

        PacResult(boolean ok, String status, String receiptId, String message) {
            this.ok = ok; this.status = status; this.receiptId = receiptId; this.message = message;
        }
        static PacResult from(CancelRequest cr) {
            return new PacResult(true, cr.getStatus(), cr.getReceiptId(), cr.getResultMessage());
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<PacResult> cancel(@RequestBody CancelRequest request) {
        CancelRequest saved = service.solicitarCancelacion(request);
        return ResponseEntity.ok(PacResult.from(saved));
    }

    @GetMapping("/status/{uuid}")
    public ResponseEntity<PacResult> status(@PathVariable("uuid") @NotBlank String uuid) {
        CancelRequest cr = service.obtenerEstado(uuid);
        if (cr == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(PacResult.from(cr));
    }
}


